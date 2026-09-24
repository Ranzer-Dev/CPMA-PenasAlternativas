import pytest
import numpy as np
import cv2
import base64
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)

def create_synthetic_face_image_base64() -> str:
    img = np.ones((200, 200, 3), dtype=np.uint8) * 128
    cv2.circle(img, (100, 100), 50, (200, 200, 200), -1)
    cv2.circle(img, (80, 85), 8, (50, 50, 50), -1)
    cv2.circle(img, (120, 85), 8, (50, 50, 50), -1)
    cv2.line(img, (100, 95), (100, 115), (50, 50, 50), 2)
    cv2.ellipse(img, (100, 125), (20, 10), 0, 0, 180, (50, 50, 50), 2)
    
    _, buffer = cv2.imencode(".jpg", img)
    return base64.b64encode(buffer).decode("utf-8")

def test_health_check():
    response = client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "ok"
    assert data["service"] == "cpma-facial-service"

def test_detect_faces_missing_input():
    response = client.post("/detect", data={})
    assert response.status_code == 422

def test_compare_faces_identical_images():
    img_b64 = create_synthetic_face_image_base64()
    payload = {
        "imageA": img_b64,
        "imageB": img_b64,
        "threshold": 0.50
    }
    response = client.post("/compare", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert "match" in data
    assert "confidence" in data
