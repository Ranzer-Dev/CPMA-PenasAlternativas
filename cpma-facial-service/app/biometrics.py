import base64
import os
from typing import List, Tuple, Optional
import cv2
import numpy as np
import onnxruntime as ort

class BiometricEngine:
    def __init__(
        self,
        yunet_path: Optional[str] = None,
        facenet_path: Optional[str] = None,
        cascade_path: Optional[str] = None
    ):
        base_data_dir = os.path.join(os.path.dirname(os.path.abspath(__file__)), "data")
        models_dir = os.path.join(base_data_dir, "models")

        if yunet_path is None:
            candidate_yunet = os.path.join(models_dir, "face_detection_yunet_2023mar.onnx")
            if os.path.exists(candidate_yunet):
                yunet_path = candidate_yunet

        if facenet_path is None:
            candidate_facenet = os.path.join(models_dir, "faceNet.onnx")
            if os.path.exists(candidate_facenet):
                facenet_path = candidate_facenet

        if cascade_path is None:
            candidate_cascade = os.path.join(base_data_dir, "haarcascade_frontalface_alt.xml")
            if not os.path.exists(candidate_cascade):
                candidate_cascade = os.path.join(base_data_dir, "haarcascade_frontalface_default.xml")
            cascade_path = candidate_cascade

        self.yunet_path = yunet_path
        self.facenet_path = facenet_path
        self.cascade_path = cascade_path

        self.detector_yunet = None
        if self.yunet_path and os.path.exists(self.yunet_path):
            try:
                self.detector_yunet = cv2.FaceDetectorYN.create(
                    self.yunet_path,
                    "",
                    (640, 480),
                    score_threshold=0.65,
                    nms_threshold=0.3,
                    top_k=5
                )
            except Exception:
                self.detector_yunet = None

        self.face_cascade = None
        if self.cascade_path and os.path.exists(self.cascade_path):
            try:
                self.face_cascade = cv2.CascadeClassifier(self.cascade_path)
            except Exception:
                self.face_cascade = None

        self.facenet_session = None
        if self.facenet_path and os.path.exists(self.facenet_path):
            try:
                self.facenet_session = ort.InferenceSession(
                    self.facenet_path,
                    providers=["CPUExecutionProvider"]
                )
            except Exception:
                self.facenet_session = None

    def decode_image_from_base64(self, base64_str: str) -> np.ndarray:
        if "," in base64_str:
            base64_str = base64_str.split(",", 1)[1]
        img_bytes = base64.b64decode(base64_str)
        nparr = np.frombuffer(img_bytes, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        if img is None:
            raise ValueError("Unable to decode image from provided base64 string")
        return img

    def decode_image_from_bytes(self, img_bytes: bytes) -> np.ndarray:
        nparr = np.frombuffer(img_bytes, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        if img is None:
            raise ValueError("Unable to decode image from provided raw bytes")
        return img

    def detect_faces(self, img: np.ndarray) -> List[Tuple[int, int, int, int]]:
        h, w = img.shape[:2]
        if self.detector_yunet is not None:
            try:
                self.detector_yunet.setInputSize((w, h))
                _, faces = self.detector_yunet.detect(img)
                if faces is not None and len(faces) > 0:
                    valid_faces = []
                    for f in faces:
                        fx, fy, fw, fh = f[0:4].astype(int)
                        score = float(f[-1])
                        if fw >= 60 and fh >= 60 and score >= 0.65:
                            aspect = float(fh) / float(fw) if fw > 0 else 0.0
                            if 0.75 <= aspect <= 1.8:
                                valid_faces.append((int(fx), int(fy), int(fw), int(fh)))
                    if valid_faces:
                        valid_faces.sort(key=lambda box: box[2] * box[3], reverse=True)
                        return valid_faces
            except Exception:
                pass

        if self.face_cascade is not None:
            gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
            faces = self.face_cascade.detectMultiScale(
                gray,
                scaleFactor=1.08,
                minNeighbors=6,
                minSize=(80, 80)
            )
            filtered = []
            for (fx, fy, fw, fh) in faces:
                aspect = float(fh) / float(fw) if fw > 0 else 0.0
                if 0.75 <= aspect <= 1.8:
                    filtered.append((int(fx), int(fy), int(fw), int(fh)))
            filtered.sort(key=lambda box: box[2] * box[3], reverse=True)
            return filtered

        return []

    def check_anti_spoofing(self, img: np.ndarray, bbox: Tuple[int, int, int, int]) -> Tuple[bool, str, float]:
        x, y, w, h = bbox
        h_img, w_img = img.shape[:2]

        pad_y = int(h * 0.08)
        pad_x = int(w * 0.08)
        y1, y2 = max(0, y - pad_y), min(h_img, y + h + pad_y)
        x1, x2 = max(0, x - pad_x), min(w_img, x + w + pad_x)
        face_roi = img[y1:y2, x1:x2]

        if face_roi.size == 0:
            return False, "ROSTO_INVALIDO", 0.0

        gray = cv2.cvtColor(face_roi, cv2.COLOR_BGR2GRAY)
        specular_ratio = float(np.sum(gray >= 252)) / float(gray.size)

        ycrcb = cv2.cvtColor(face_roi, cv2.COLOR_BGR2YCrCb)
        cr = ycrcb[:, :, 1]
        cb = ycrcb[:, :, 2]
        skin_mask = (cr >= 128) & (cr <= 178) & (cb >= 70) & (cb <= 135)
        skin_ratio = float(np.sum(skin_mask)) / float(face_roi.shape[0] * face_roi.shape[1])

        margin_y = int(h * 0.9)
        margin_x = int(w * 0.9)
        my1, my2 = max(0, y - margin_y), min(h_img, y + h + margin_y)
        mx1, mx2 = max(0, x - margin_x), min(w_img, x + w + margin_x)
        outer_roi = img[my1:my2, mx1:mx2]

        phone_bezel_detected = False
        if outer_roi.size > 0:
            outer_gray = cv2.cvtColor(outer_roi, cv2.COLOR_BGR2GRAY)
            edges = cv2.Canny(outer_gray, 40, 140)
            contours, _ = cv2.findContours(edges, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
            for cnt in contours:
                peri = cv2.arcLength(cnt, True)
                approx = cv2.approxPolyDP(cnt, 0.03 * peri, True)
                if len(approx) == 4 and cv2.isContourConvex(approx):
                    _, _, bw, bh = cv2.boundingRect(approx)
                    if bh > 0 and bw > 0:
                        aspect = float(bh) / float(bw) if bh >= bw else float(bw) / float(bh)
                        if 1.4 <= aspect <= 2.45 and bw >= (w * 0.7):
                            phone_bezel_detected = True
                            break

        if phone_bezel_detected:
            return False, "MOLDURA_DE_DISPOSITIVO_DETECTADA", 0.15

        if specular_ratio > 0.075:
            return False, "REFLEXO_ESPECULAR_DE_TELA_DETECTADO", 0.25

        if skin_ratio < 0.25:
            return False, "DISTRIBUICAO_DE_PELE_INCOMPATIVEL", 0.35

        confidence_liveness = min(1.0, 0.5 + (skin_ratio * 0.5) - (specular_ratio * 2.0))
        return True, "VIVACIDADE_CONFIRMADA", round(confidence_liveness, 4)

    def extract_face_crop(self, img: np.ndarray, bbox: Tuple[int, int, int, int], size: Tuple[int, int] = (160, 160)) -> np.ndarray:
        x, y, w, h = bbox
        h_img, w_img = img.shape[:2]
        pad_y = int(h * 0.08)
        pad_x = int(w * 0.08)
        y1, y2 = max(0, y - pad_y), min(h_img, y + h + pad_y)
        x1, x2 = max(0, x - pad_x), min(w_img, x + w + pad_x)
        face_crop = img[y1:y2, x1:x2]
        if face_crop.size == 0:
            face_crop = img[y:y+h, x:x+w]
        return cv2.resize(face_crop, size, interpolation=cv2.INTER_AREA)

    def get_facenet_embedding(self, face_crop: np.ndarray) -> Optional[np.ndarray]:
        if self.facenet_session is None:
            return None
        try:
            resized = cv2.resize(face_crop, (160, 160))
            rgb = cv2.cvtColor(resized, cv2.COLOR_BGR2RGB)
            mean, std = np.mean(rgb), np.std(rgb)
            std_adj = np.maximum(std, 1.0 / np.sqrt(rgb.size))
            norm_img = (rgb - mean) / std_adj
            tensor = np.expand_dims(norm_img.astype(np.float32), axis=0)
            emb = self.facenet_session.run(["Bottleneck_BatchNorm"], {"image_input": tensor})[0][0]
            norm = np.linalg.norm(emb)
            if norm > 0:
                emb = emb / norm
            return emb
        except Exception:
            return None

    def compare_faces(self, img_a: np.ndarray, img_b: np.ndarray, threshold: float = 0.65) -> Tuple[bool, float]:
        faces_a = self.detect_faces(img_a)
        faces_b = self.detect_faces(img_b)

        if not faces_a or not faces_b:
            return False, 0.0

        primary_a = faces_a[0]
        primary_b = faces_b[0]

        is_live_a, reason_a, _ = self.check_anti_spoofing(img_a, primary_a)
        if not is_live_a:
            return False, 0.0

        face_crop_a = self.extract_face_crop(img_a, primary_a, size=(160, 160))
        face_crop_b = self.extract_face_crop(img_b, primary_b, size=(160, 160))

        emb_a = self.get_facenet_embedding(face_crop_a)
        emb_b = self.get_facenet_embedding(face_crop_b)

        if emb_a is not None and emb_b is not None:
            cos_sim = float(np.dot(emb_a, emb_b))
            norm_score = max(0.0, min(1.0, cos_sim))
            match = norm_score >= threshold
            return match, round(norm_score, 4)

        gray_a = cv2.cvtColor(face_crop_a, cv2.COLOR_BGR2GRAY)
        gray_b = cv2.cvtColor(face_crop_b, cv2.COLOR_BGR2GRAY)
        gray_a = cv2.equalizeHist(gray_a)
        gray_b = cv2.equalizeHist(gray_b)
        hist_a = cv2.calcHist([gray_a], [0], None, [256], [0, 256])
        hist_b = cv2.calcHist([gray_b], [0], None, [256], [0, 256])
        cv2.normalize(hist_a, hist_a, alpha=0, beta=1, norm_type=cv2.NORM_MINMAX)
        cv2.normalize(hist_b, hist_b, alpha=0, beta=1, norm_type=cv2.NORM_MINMAX)
        score = cv2.compareHist(hist_a, hist_b, cv2.HISTCMP_CORREL)
        normalized_score = max(0.0, float(score))
        match = normalized_score >= threshold
        return match, round(normalized_score, 4)
