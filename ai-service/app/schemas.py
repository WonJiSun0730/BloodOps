from typing import Literal

from pydantic import BaseModel, Field


class ConversationContext(BaseModel):
    last_intent: str | None = Field(
        default=None,
        max_length=100,
    )
    last_keyword: str | None = Field(
        default=None,
        max_length=200,
    )


class AnalyzeRequest(BaseModel):
    message: str = Field(
        min_length=1,
        max_length=1000,
    )
    context: ConversationContext | None = None


class AnalyzeResponse(BaseModel):
    intent: str
    confidence: float = Field(
        ge=0.0,
        le=1.0,
    )
    keyword: str | None = None
    knowledge_id: str | None = None
    answer: str | None = None
    sources: list[str] = Field(
        default_factory=list,
    )
    matched_by: Literal[
        "RULE",
        "SIMILARITY",
        "HYBRID",
        "DOMAIN_FEATURE",
        "FALLBACK",
    ]