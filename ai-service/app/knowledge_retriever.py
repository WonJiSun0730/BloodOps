import json
from dataclasses import dataclass
from pathlib import Path

from sklearn.feature_extraction.text import (
    TfidfVectorizer,
)
from sklearn.metrics.pairwise import (
    cosine_similarity,
)

from app.normalizer import normalize_text


DATA_FILE_PATH = (
    Path(__file__).resolve().parent.parent
    / "data"
    / "knowledge.json"
)


@dataclass(frozen=True)
class KnowledgeMatch:
    knowledge_id: str
    answer: str
    source: str
    source_url: str
    similarity: float


class DomainKnowledgeRetriever:
    MINIMUM_SIMILARITY = 0.30

    def __init__(self) -> None:
        (
            self.documents,
            self.document_entries,
        ) = self._load_documents()

        self.vectorizer = TfidfVectorizer(
            analyzer="char_wb",
            ngram_range=(2, 5),
            sublinear_tf=True,
            lowercase=False,
        )

        self.document_vectors = (
            self.vectorizer.fit_transform(
                self.documents
            )
        )

    def search(
        self,
        message: str,
    ) -> KnowledgeMatch | None:
        normalized = normalize_text(message)

        if not normalized:
            return None

        message_vector = self.vectorizer.transform(
            [normalized]
        )

        similarities = cosine_similarity(
            message_vector,
            self.document_vectors,
        )[0]

        candidate_indices = [
            index
            for index, entry
            in enumerate(self.document_entries)
            if self._matches_required_keyword(
                normalized,
                entry["keywords"],
            )
        ]

        if not candidate_indices:
            return None

        best_index = max(
            candidate_indices,
            key=lambda index: similarities[index],
        )

        best_similarity = float(
            similarities[best_index]
        )

        if (
            best_similarity
            < self.MINIMUM_SIMILARITY
        ):
            return None

        entry = self.document_entries[
            best_index
        ]

        return KnowledgeMatch(
            knowledge_id=entry["id"],
            answer=entry["answer"],
            source=entry["source"],
            source_url=entry["source_url"],
            similarity=round(
                best_similarity,
                2,
            ),
        )

    def find_by_id(
        self,
        knowledge_id: str,
    ) -> KnowledgeMatch | None:
        for entry in self.document_entries:
            if entry["id"] != knowledge_id:
                continue

            return KnowledgeMatch(
                knowledge_id=entry["id"],
                answer=entry["answer"],
                source=entry["source"],
                source_url=entry["source_url"],
                similarity=1.0,
            )

        return None

    @staticmethod
    def _matches_required_keyword(
        normalized_message: str,
        keywords: list[str],
    ) -> bool:
        return any(
            normalize_text(keyword)
            in normalized_message
            for keyword in keywords
        )

    def _load_documents(
        self,
    ) -> tuple[
        list[str],
        list[dict[str, str]],
    ]:
        with DATA_FILE_PATH.open(
            encoding="utf-8"
        ) as file:
            knowledge_entries = json.load(file)

        documents: list[str] = []
        document_entries: list[dict[str, str]] = []

        for entry in knowledge_entries:
            for question in entry["questions"]:
                documents.append(
                    normalize_text(question)
                )
                document_entries.append(entry)

        if not documents:
            raise ValueError(
                "헌혈 지식 데이터가 비어 있습니다."
            )

        return documents, document_entries