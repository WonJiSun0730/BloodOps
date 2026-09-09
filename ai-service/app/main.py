from fastapi import FastAPI

from app.schemas import (
    AnalyzeRequest,
    AnalyzeResponse,
)


app = FastAPI(
    title="BloodOps AI Service",
    description=(
        "BloodOps 도메인에 특화된 "
        "의도 분류 및 지식 검색 서비스"
    ),
    version="1.0.0",
)


@app.get("/health")
def health() -> dict[str, str]:
    return {
        "status": "UP",
        "service": "bloodops-ai",
    }


@app.post(
    "/analyze",
    response_model=AnalyzeResponse,
)
def analyze(
    request: AnalyzeRequest,
) -> AnalyzeResponse:
    """
    현재 단계에서는 API 계약만 검증합니다.

    실제 의도 분류기는 다음 구현 단계에서 연결합니다.
    """
    return AnalyzeResponse(
        intent="OTHER",
        confidence=0.0,
        keyword=None,
        knowledge_id=None,
        matched_by="FALLBACK",
    )