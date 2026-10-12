from typing import List, Optional
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
    isLive: Optional[bool] = True
    spoofReason: Optional[str] = None

class CompareFacesRequest(BaseModel):
    imageA: str
    imageB: str
    threshold: float = 0.65

class CompareFacesResponse(BaseModel):
    match: bool
    confidence: float
    threshold: float
    isLive: Optional[bool] = True
    spoofReason: Optional[str] = None
