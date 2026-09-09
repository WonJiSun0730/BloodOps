from fastapi import FastAPI

from app.hybrid_classifier import (
    HybridIntentClassifier,
)
from app.intents import Intent
from app.knowledge_retriever import (
    DomainKnowledgeRetriever,
)
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

intent_classifier = HybridIntentClassifier()
knowledge_retriever = DomainKnowledgeRetriever()


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
    knowledge_match = knowledge_retriever.search(
        request.message
    )

    if knowledge_match is not None:
        return AnalyzeResponse(
            intent=Intent.GENERAL_DONATION.value,
            confidence=knowledge_match.similarity,
            keyword=None,
            knowledge_id=(
                knowledge_match.knowledge_id
            ),
            answer=knowledge_match.answer,
            sources=[
                knowledge_match.source,
            ],
            matched_by="SIMILARITY",
        )

    classification = intent_classifier.classify(
        message=request.message,
        context=request.context,
    )

    return AnalyzeResponse(
        intent=classification.intent.value,
        confidence=classification.confidence,
        keyword=classification.keyword,
        knowledge_id=classification.knowledge_id,
        answer=None,
        sources=[],
        matched_by=classification.matched_by,
    )