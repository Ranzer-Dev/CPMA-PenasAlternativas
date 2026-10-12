from fastapi import FastAPI, HTTPException, UploadFile, File, Form
from fastapi.responses import StreamingResponse
from fastapi.middleware.cors import CORSMiddleware
from typing import Optional
import base64
import cv2
import threading
import time
import uvicorn

from app.schemas import DetectFacesResponse, BoundingBox, CompareFacesRequest, CompareFacesResponse
from app.biometrics import BiometricEngine

app = FastAPI(
    title="CPMA Biometric Facial Service",
    description="Microservico local offline para deteccao e comparacao biometrica facial",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

engine = BiometricEngine()

class CameraDevice:
    _instance = None
    _lock = threading.Lock()

    def __new__(cls):
        with cls._lock:
            if cls._instance is None:
                cls._instance = super().__new__(cls)
                cls._instance.cap = None
                cls._instance.stream_lock = threading.Lock()
                cls._instance.last_clean_frame = None
            return cls._instance

    def _open_device(self):
        if self.cap is None or not self.cap.isOpened():
            self.cap = cv2.VideoCapture(0, cv2.CAP_DSHOW)
            if not self.cap.isOpened():
                self.cap = cv2.VideoCapture(0)

    def release_device(self):
        with self.stream_lock:
            if self.cap is not None:
                self.cap.release()
                self.cap = None

    def read_live_frame(self):
        with self.stream_lock:
            self._open_device()
            if not self.cap.isOpened():
                return False, None, 0
            ret, frame = self.cap.read()
            if not ret or frame is None:
                return False, None, 0

            clean_frame = frame.copy()
            self.last_clean_frame = clean_frame

            faces = engine.detect_faces(frame)
            annotated_frame = frame
            for idx, (x, y, w, h) in enumerate(faces):
                if idx == 0:
                    is_live, reason, _ = engine.check_anti_spoofing(frame, (x, y, w, h))
                    color = (0, 220, 0) if is_live else (0, 0, 235)
                    label = "Rosto Autentico" if is_live else f"Alerta Spoofing: {reason}"
                    cv2.rectangle(annotated_frame, (x, y), (x + w, y + h), color, 2)
                    cv2.putText(annotated_frame, label, (x, max(22, y - 8)), cv2.FONT_HERSHEY_SIMPLEX, 0.52, color, 2)
                else:
                    cv2.rectangle(annotated_frame, (x, y), (x + w, y + h), (160, 160, 160), 1)

            _, buffer = cv2.imencode(".jpg", annotated_frame, [cv2.IMWRITE_JPEG_QUALITY, 80])
            return True, buffer.tobytes(), len(faces)

    def capture_snapshot(self):
        with self.stream_lock:
            if self.last_clean_frame is not None:
                frame = self.last_clean_frame.copy()
            else:
                self._open_device()
                if not self.cap.isOpened():
                    return None
                ret, frame = self.cap.read()
                if not ret or frame is None:
                    return None

            faces = engine.detect_faces(frame)
            is_live = True
            spoof_reason = None
            if faces:
                is_live, spoof_reason, _ = engine.check_anti_spoofing(frame, faces[0])

            _, buffer = cv2.imencode(".jpg", frame, [cv2.IMWRITE_JPEG_QUALITY, 92])
            raw_b64 = base64.b64encode(buffer).decode("utf-8")

            return {
                "success": True,
                "base64": f"data:image/jpeg;base64,{raw_b64}",
                "rawBase64": raw_b64,
                "faceDetected": len(faces) > 0,
                "count": len(faces),
                "isLive": is_live,
                "spoofReason": spoof_reason,
                "boundingBoxes": [BoundingBox(x=x, y=y, width=w, height=h) for (x, y, w, h) in faces]
            }

camera_device = CameraDevice()

def generate_mjpeg_stream():
    try:
        while True:
            success, frame_bytes, _ = camera_device.read_live_frame()
            if not success or frame_bytes is None:
                time.sleep(0.05)
                continue
            yield (b"--frame\r\n"
                   b"Content-Type: image/jpeg\r\n\r\n" + frame_bytes + b"\r\n")
            time.sleep(0.033)
    finally:
        camera_device.release_device()

@app.get("/health")
def health_check():
    return {
        "status": "ok",
        "service": "cpma-facial-service",
        "version": "1.0.0"
    }

@app.get("/camera/stream")
def get_camera_stream():
    return StreamingResponse(
        generate_mjpeg_stream(),
        media_type="multipart/x-mixed-replace; boundary=frame"
    )

@app.get("/camera/capture")
def capture_from_camera():
    result = camera_device.capture_snapshot()
    if result is None:
        raise HTTPException(status_code=500, detail="Nao foi possivel acessar a webcam local.")
    return result

@app.post("/camera/release")
def release_camera():
    camera_device.release_device()
    return {"status": "released"}

@app.post("/detect", response_model=DetectFacesResponse)
async def detect_faces(
    file: Optional[UploadFile] = File(None),
    base64_image: Optional[str] = Form(None)
):
    try:
        if file is not None:
            content = await file.read()
            img = engine.decode_image_from_bytes(content)
        elif base64_image:
            img = engine.decode_image_from_base64(base64_image)
        else:
            raise HTTPException(status_code=422, detail="Nenhuma imagem fornecida via arquivo ou base64.")

        faces = engine.detect_faces(img)
        is_live = True
        spoof_reason = None
        if faces:
            is_live, spoof_reason, _ = engine.check_anti_spoofing(img, faces[0])
        bboxes = [BoundingBox(x=x, y=y, width=w, height=h) for (x, y, w, h) in faces]

        return DetectFacesResponse(
            faceDetected=len(faces) > 0,
            count=len(faces),
            boundingBoxes=bboxes,
            isLive=is_live,
            spoofReason=spoof_reason
        )
    except HTTPException:
        raise
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Erro interno de processamento facial: {str(e)}")

@app.post("/compare", response_model=CompareFacesResponse)
def compare_faces(request: CompareFacesRequest):
    try:
        img_a = engine.decode_image_from_base64(request.imageA)
        img_b = engine.decode_image_from_base64(request.imageB)

        match, confidence = engine.compare_faces(img_a, img_b, threshold=request.threshold)

        faces_a = engine.detect_faces(img_a)
        is_live = True
        spoof_reason = None
        if faces_a:
            is_live, spoof_reason, _ = engine.check_anti_spoofing(img_a, faces_a[0])

        return CompareFacesResponse(
            match=match,
            confidence=confidence,
            threshold=request.threshold,
            isLive=is_live,
            spoofReason=spoof_reason
        )
    except HTTPException:
        raise
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Erro ao comparar faces: {str(e)}")

if __name__ == "__main__":
    uvicorn.run(app, host="127.0.0.1", port=8001, log_level="info")
