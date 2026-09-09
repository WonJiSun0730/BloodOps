from app.classifier import (
    ClassificationResult,
    DomainIntentClassifier,
)
from app.intents import Intent
from app.schemas import ConversationContext
from app.similarity_classifier import (
    SimilarityIntentClassifier,
)


class HybridIntentClassifier:
    SIMILARITY_MIN_CONFIDENCE = 0.45

    def __init__(self) -> None:
        self.rule_classifier = (
            DomainIntentClassifier()
        )
        self.similarity_classifier = (
            SimilarityIntentClassifier()
        )

    def classify(
        self,
        message: str,
        context: ConversationContext | None = None,
    ) -> ClassificationResult:
        rule_result = self.rule_classifier.classify(
            message=message,
            context=context,
        )

        similarity_result = (
            self.similarity_classifier.classify(
                message=message
            )
        )

        if rule_result.intent != Intent.OTHER:
            if (
                similarity_result.intent
                == rule_result.intent
            ):
                return ClassificationResult(
                    intent=rule_result.intent,
                    confidence=max(
                        rule_result.confidence,
                        similarity_result.confidence,
                    ),
                    keyword=rule_result.keyword,
                    knowledge_id=None,
                    matched_by="HYBRID",
                )

            return rule_result

        if (
            similarity_result.intent
            == Intent.OTHER
        ):
            return similarity_result

        if (
            similarity_result.confidence
            < self.SIMILARITY_MIN_CONFIDENCE
        ):
            return ClassificationResult(
                intent=Intent.OTHER,
                confidence=0.0,
                keyword=None,
                knowledge_id=None,
                matched_by="FALLBACK",
            )

        return similarity_result