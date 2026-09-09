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

        return similarity_result