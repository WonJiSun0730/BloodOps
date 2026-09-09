from dataclasses import dataclass

from app.intents import Intent
from app.normalizer import compact_text


@dataclass(frozen=True)
class DomainFeatureMatch:
    intent: Intent
    knowledge_id: str | None
    confidence: float


class DomainFeatureAnalyzer:
    DONATION_ACTIONS = (
        "헌혈",
        "피뽑",
        "채혈",
    )

    AFTER_DONATION_MARKERS = (
        "헌혈후",
        "헌혈뒤",
        "헌혈다음",
        "헌혈당일",
        "헌혈한날",
        "헌혈하고",
        "헌혈하고나서",
        "헌혈했는데",
        "헌혈한뒤",
        "피뽑은날",
        "피뽑은뒤",
        "피뽑고",
        "채혈후",
        "채혈뒤",
        "채혈하고",
    )

    ALCOHOL_FEATURES = (
        "술",
        "음주",
        "맥주",
        "소주",
        "와인",
        "막걸리",
        "회식",
        "한잔",
        "알코올",
    )

    INFECTION_FEATURES = (
        "코로나",
        "독감",
        "감염",
        "감염병",
        "전염병",
        "확진",
        "양성",
        "판정",
    )

    SYMPTOM_FEATURES = (
       "아파",
       "아픈",
       "몸이안좋",
       "상태가안좋",
       "어지러",
       "어지럽",
       "현기증",
       "울렁",
       "메스꺼",
       "식은땀",
       "기운없",
       "기운없",
       "기운이없",
       "힘이없",
       "힘이없",
       "머리아파",
       "머리가아파",
       "두통",
       "실신",
       "쓰러질",
       )

    NUTRITION_FEATURES = (
        "밥",
        "식사",
        "뭐먹",
        "먹는게",
        "추천음식",
        "단백질",
        "물",
        "물마셔",
        "물많이",
        "수분",
        "수분",
        "이온음료",
        "간식",
    )

    def analyze(
        self,
        message: str,
    ) -> DomainFeatureMatch | None:
        compact = compact_text(message)

        if not self._contains_any(
            compact,
            self.DONATION_ACTIONS,
        ):
            return None

        if not self._contains_any(
            compact,
            self.AFTER_DONATION_MARKERS,
        ):
            return None

        if self._contains_any(
            compact,
            self.INFECTION_FEATURES,
        ):
            return DomainFeatureMatch(
                intent=Intent.BLOOD_HOLD_GUIDE,
                knowledge_id=None,
                confidence=0.99,
            )

        if self._contains_any(
            compact,
            self.SYMPTOM_FEATURES,
        ):
            return DomainFeatureMatch(
                intent=Intent.GENERAL_DONATION,
                knowledge_id=(
                    "DONATION_AFTER_DIZZINESS"
                ),
                confidence=0.99,
            )

        if self._contains_any(
            compact,
            self.ALCOHOL_FEATURES,
        ):
            return DomainFeatureMatch(
                intent=Intent.GENERAL_DONATION,
                knowledge_id=(
                    "DONATION_AFTER_ALCOHOL"
                ),
                confidence=0.99,
            )

        if self._contains_any(
            compact,
            self.NUTRITION_FEATURES,
        ):
            return DomainFeatureMatch(
                intent=Intent.GENERAL_DONATION,
                knowledge_id=(
                    "DONATION_AFTER_NUTRITION"
                ),
                confidence=0.99,
            )

        return None

    @staticmethod
    def _contains_any(
        message: str,
        features: tuple[str, ...],
    ) -> bool:
        return any(
            feature in message
            for feature in features
        )