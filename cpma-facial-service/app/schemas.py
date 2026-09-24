from typing import List
from pydantic import BaseModel

class BoundingBox(BaseModel):
    x: int
    y: int
    width: int
    height: int

class DetectFacesResponse(BaseModel):
    faceDetected: bool
    count: int
    boundingBoxes: List[BoundingBox]

class CompareFacesRequest(BaseModel):
    imageA: str
    imageB: str
    threshold: float = 0.70

class CompareFacesResponse(BaseModel):
    match: bool
    confidence: float
    threshold: float
