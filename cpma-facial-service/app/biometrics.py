import base64
import os
from typing import List, Tuple, Optional
import cv2
import numpy as np

class BiometricEngine:
    def __init__(self, cascade_path: Optional[str] = None):
        if cascade_path is None:
            current_dir = os.path.dirname(os.path.abspath(__file__))
            cascade_path = os.path.join(current_dir, "data", "haarcascade_frontalface_default.xml")
        
        self.cascade_path = cascade_path
        if not os.path.exists(cascade_path):
            raise FileNotFoundError(f"Cascade classifier not found at {cascade_path}")
        
        self.face_cascade = cv2.CascadeClassifier(cascade_path)

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
        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        gray = cv2.equalizeHist(gray)
        faces = self.face_cascade.detectMultiScale(
            gray,
            scaleFactor=1.1,
            minNeighbors=5,
            minSize=(30, 30)
        )
        return [(int(x), int(y), int(w), int(h)) for (x, y, w, h) in faces]

    def extract_face_crop(self, img: np.ndarray, bbox: Tuple[int, int, int, int], size: Tuple[int, int] = (160, 160)) -> np.ndarray:
        x, y, w, h = bbox
        face_crop = img[y:y+h, x:x+w]
        return cv2.resize(face_crop, size, interpolation=cv2.INTER_AREA)

    def compare_faces(self, img_a: np.ndarray, img_b: np.ndarray, threshold: float = 0.70) -> Tuple[bool, float]:
        faces_a = self.detect_faces(img_a)
        faces_b = self.detect_faces(img_b)

        if not faces_a or not faces_b:
            return False, 0.0

        face_crop_a = self.extract_face_crop(img_a, faces_a[0])
        face_crop_b = self.extract_face_crop(img_b, faces_b[0])

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
