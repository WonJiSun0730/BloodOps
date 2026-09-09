from fastapi import FastAPI

from app.classifier import DomainIntentClassifier
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

intent_classifier = DomainIntentClassifier()


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
    classification = intent_classifier.classify(
        message=request.message,
        context=request.context,
    )

    return AnalyzeResponse(
        intent=classification.intent.value,
        confidence=classification.confidence,
        keyword=classification.keyword,
        knowledge_id=classification.knowledge_id,
        matched_by=classification.matched_by,
    )