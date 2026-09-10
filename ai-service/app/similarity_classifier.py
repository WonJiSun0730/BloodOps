import json
from pathlib import Path

from sklearn.feature_extraction.text import (
    TfidfVectorizer,
)
from sklearn.metrics.pairwise import (
    cosine_similarity,
)

from app.classifier import ClassificationResult
from app.intents import Intent
from app.normalizer import normalize_text


DATA_FILE_PATH = (
    Path(__file__).resolve().parent.parent
    / "data"
    / "intent_examples.json"
)


class SimilarityIntentClassifier:
    MINIMUM_SIMILARITY = 0.28

    def __init__(self) -> None:
        (
            self.example_texts,
            self.example_intents,
        ) = self._load_examples()

        self.vectorizer = TfidfVectorizer(
            analyzer="char_wb",
            ngram_range=(2, 5),
            sublinear_tf=True,
            lowercase=False,
        )

        self.example_vectors = (
            self.vectorizer.fit_transform(
                self.example_texts
            )
        )

    def classify(
        self,
        message: str,
    ) -> ClassificationResult:
        normalized = normalize_text(message)

        if not normalized:
            return self._fallback()

        message_vector = self.vectorizer.transform(
            [normalized]
        )

        similarities = cosine_similarity(
            message_vector,
            self.example_vectors,
        )[0]

        best_index = int(similarities.argmax())
        best_similarity = float(
            similarities[best_index]
        )
        best_intent = self.example_intents[
            best_index
        ]

        if (
            best_similarity
            < self.MINIMUM_SIMILARITY
        ):
            return self._fallback()

        if best_intent == Intent.OTHER:
            return ClassificationResult(
                intent=Intent.OTHER,
                confidence=round(
                    best_similarity,
                    2,
                ),
                keyword=None,
                knowledge_id=None,
                matched_by="SIMILARITY",
            )

        return ClassificationResult(
            intent=best_intent,
            confidence=round(
                best_similarity,
                2,
            ),
            keyword=None,
            knowledge_id=None,
            matched_by="SIMILARITY",
        )

    def _load_examples(
        self,
    ) -> tuple[list[str], list[Intent]]:
        with DATA_FILE_PATH.open(
            encoding="utf-8"
        ) as file:
            intent_groups = json.load(file)

        texts: list[str] = []
        intents: list[Intent] = []

        for group in intent_groups:
            intent = Intent(group["intent"])

            for example in group["examples"]:
                texts.append(
                    normalize_text(example)
                )
                intents.append(intent)

        if not texts:
            raise ValueError(
                "Intent 학습 예문이 비어 있습니다."
            )

        return texts, intents

    @staticmethod
    def _fallback() -> ClassificationResult:
        return ClassificationResult(
            intent=Intent.OTHER,
            confidence=0.0,
            keyword=None,
            knowledge_id=None,
            matched_by="FALLBACK",
        )