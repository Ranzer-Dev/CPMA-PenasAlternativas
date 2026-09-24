from fastapi import FastAPI, HTTPException, UploadFile, File, Form
from fastapi.middleware.cors import CORSMiddleware
from typing import Optional
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

@app.get("/health")
def health_check():
    return {
        "status": "ok",
        "service": "cpma-facial-service",
        "version": "1.0.0"
    }

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
        bboxes = [BoundingBox(x=x, y=y, width=w, height=h) for (x, y, w, h) in faces]

        return DetectFacesResponse(
            faceDetected=len(faces) > 0,
            count=len(faces),
            boundingBoxes=bboxes
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

        return CompareFacesResponse(
            match=match,
            confidence=confidence,
            threshold=request.threshold
        )
    except HTTPException:
        raise
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Erro ao comparar faces: {str(e)}")

if __name__ == "__main__":
    uvicorn.run(app, host="127.0.0.1", port=8001, log_level="info")
