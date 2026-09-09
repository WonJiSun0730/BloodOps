from dataclasses import dataclass

from app.intents import Intent
from app.normalizer import (
    compact_text,
    normalize_text,
)
from app.schemas import ConversationContext


@dataclass(frozen=True)
class IntentRule:
    intent: Intent
    strong_phrases: tuple[str, ...]
    keywords: tuple[str, ...]
    minimum_score: float
    priority: int


@dataclass(frozen=True)
class ClassificationResult:
    intent: Intent
    confidence: float
    keyword: str | None
    knowledge_id: str | None
    matched_by: str


RULES = (
    IntentRule(
        intent=Intent.CERTIFICATE_VERIFY,
        strong_phrases=(
            "증명서 진위확인",
            "진위확인",
            "발급번호 확인",
            "증명서 진짜",
        ),
        keywords=(
            "증명서",
            "진위",
            "발급번호",
            "확인",
        ),
        minimum_score=4.0,
        priority=100,
    ),
    IntentRule(
        intent=Intent.BLOOD_HOLD_CHECK,
        strong_phrases=(
            "혈액사용보류 확인",
            "보류신청 확인",
            "보류 상태",
            "보류상태",
        ),
        keywords=(
            "보류",
            "신청",
            "상태",
            "확인",
        ),
        minimum_score=4.0,
        priority=95,
    ),
    IntentRule(
        intent=Intent.CENTER_EARLIEST_AVAILABLE,
        strong_phrases=(
            "가장 빠르게 예약 가능한 센터",
            "가장 먼저 예약 가능한 곳",
            "제일 빠른 곳",
            "그중 가장 빠른",
            "그중 어디가 제일 빨라",
        ),
        keywords=(
            "가장",
            "제일",
            "빠른",
            "먼저",
            "예약가능",
            "그중",
        ),
        minimum_score=4.5,
        priority=90,
    ),
    IntentRule(
        intent=Intent.RESERVATION_CURRENT,
        strong_phrases=(
            "내 예약",
            "예약내역",
            "예약확인",
            "예약조회",
            "예약 변경",
            "예약 취소",
            "예약 보여",
            "예약 뭐였",
            "예약 머였",
        ),
        keywords=(
            "예약",
            "내역",
            "확인",
            "조회",
            "변경",
            "취소",
        ),
        minimum_score=4.0,
        priority=85,
    ),
    IntentRule(
        intent=Intent.DONATION_HISTORY,
        strong_phrases=(
            "내 헌혈",
            "헌혈기록",
            "헌혈내역",
            "최근 헌혈",
        ),
        keywords=(
            "헌혈",
            "기록",
            "내역",
            "최근",
        ),
        minimum_score=4.0,
        priority=80,
    ),
    IntentRule(
        intent=Intent.BLOOD_HOLD_GUIDE,
        strong_phrases=(
            "혈액사용보류",
            "보류신청",
            "헌혈한 피 쓰지",
            "헌혈한 혈액 쓰지",
            "헌혈 후 감염병",
            "헌혈하고 코로나",
            "헌혈 후 코로나",
        ),
        keywords=(
            "보류",
            "헌혈",
            "감염병",
            "코로나",
            "사용",
        ),
        minimum_score=4.0,
        priority=78,
    ),
    IntentRule(
        intent=Intent.CERTIFICATE_ISSUE,
        strong_phrases=(
            "헌혈확인증명서",
            "헌혈 증명서",
            "증명서 발급",
            "증명서 출력",
            "증명서 저장",
            "증명서 뽑",
        ),
        keywords=(
            "증명서",
            "발급",
            "출력",
            "pdf",
        ),
        minimum_score=4.0,
        priority=75,
    ),
    IntentRule(
        intent=Intent.BLOOD_STOCK_SHORTAGE,
        strong_phrases=(
            "혈액 부족",
            "피가 부족",
            "피 부족",
            "부족한 혈액형",
            "혈액이 모자",
            "피가 모자",
            "수급 위기",
        ),
        keywords=(
            "혈액",
            "피",
            "부족",
            "모자",
            "위기",
        ),
        minimum_score=4.0,
        priority=72,
    ),
    IntentRule(
        intent=Intent.BLOOD_STOCK_STATUS,
        strong_phrases=(
            "혈액 보유량",
            "혈액보유량",
            "혈액 재고",
            "혈액재고",
            "혈액 보유현황",
            "피 얼마나",
        ),
        keywords=(
            "혈액",
            "피",
            "보유량",
            "재고",
            "현황",
        ),
        minimum_score=4.0,
        priority=70,
    ),
    IntentRule(
        intent=Intent.PROMOTION_CURRENT,
        strong_phrases=(
            "진행 중인 프로모션",
            "진행 중인 이벤트",
            "현재 프로모션",
            "현재 이벤트",
            "지금 이벤트",
            "행사 있어",
            "뭐 주는 행사",
        ),
        keywords=(
            "프로모션",
            "이벤트",
            "행사",
            "경품",
            "응모",
            "혜택",
        ),
        minimum_score=4.0,
        priority=68,
    ),
    IntentRule(
        intent=Intent.QUESTIONNAIRE,
        strong_phrases=(
            "전자문진",
            "문진 작성",
            "문진 어디",
            "문진 어떻게",
        ),
        keywords=(
            "문진",
            "작성",
            "전자문진",
        ),
        minimum_score=3.5,
        priority=65,
    ),
    IntentRule(
        intent=Intent.TEST_RESULT,
        strong_phrases=(
            "혈액검사결과",
            "검사결과",
            "헌혈검사",
            "검사 어디",
        ),
        keywords=(
            "검사",
            "결과",
            "혈액검사",
        ),
        minimum_score=3.5,
        priority=63,
    ),
    IntentRule(
        intent=Intent.RESERVATION_CREATE,
        strong_phrases=(
            "헌혈 예약",
            "예약하고",
            "예약하려",
            "예약 방법",
            "예약 어떻게",
            "예약 어디",
        ),
        keywords=(
            "헌혈",
            "예약",
            "방법",
            "신청",
        ),
        minimum_score=4.0,
        priority=60,
    ),
    IntentRule(
        intent=Intent.CENTER_SEARCH,
        strong_phrases=(
            "헌혈의집",
            "헌혈센터",
            "센터 찾",
            "센터 검색",
            "어디서 헌혈",
            "어디 가서 헌혈",
        ),
        keywords=(
            "헌혈의집",
            "헌혈센터",
            "센터",
            "위치",
            "지역",
        ),
        minimum_score=4.0,
        priority=58,
    ),
    IntentRule(
        intent=Intent.GENERAL_DONATION,
        strong_phrases=(
            "헌혈 전",
            "헌혈 후",
            "헌혈하고",
            "전혈이 뭐",
            "혈장이 뭐",
            "혈소판이 뭐",
            "헌혈 종류",
        ),
        keywords=(
            "헌혈",
            "전혈",
            "혈장",
            "혈소판",
            "음주",
            "술",
            "운동",
            "식사",
        ),
        minimum_score=3.5,
        priority=30,
    ),
)


class DomainIntentClassifier:
    STRONG_PHRASE_SCORE = 5.0
    KEYWORD_SCORE = 1.5
    CONTEXT_SCORE = 2.0

    CONTEXT_REFERENCE_WORDS = (
        "그중",
        "거기",
        "그곳",
        "아까",
        "그 센터",
    )

    def classify(
        self,
        message: str,
        context: ConversationContext | None = None,
    ) -> ClassificationResult:
        normalized = normalize_text(message)
        compact = compact_text(message)

        candidates: list[
            tuple[float, int, IntentRule]
        ] = []

        for rule in RULES:
            score = self._calculate_score(
                rule=rule,
                normalized=normalized,
                compact=compact,
                context=context,
            )

            if score >= rule.minimum_score:
                candidates.append(
                    (
                        score,
                        rule.priority,
                        rule,
                    )
                )

        if not candidates:
            return ClassificationResult(
                intent=Intent.OTHER,
                confidence=0.0,
                keyword=None,
                knowledge_id=None,
                matched_by="FALLBACK",
            )

        candidates.sort(
            key=lambda candidate: (
                candidate[0],
                candidate[1],
            ),
            reverse=True,
        )

        best_score, _, best_rule = candidates[0]

        confidence = min(
            0.99,
            round(
                0.45
                + best_score * 0.07,
                2,
            ),
        )

        keyword = self._resolve_context_keyword(
            intent=best_rule.intent,
            context=context,
        )

        return ClassificationResult(
            intent=best_rule.intent,
            confidence=confidence,
            keyword=keyword,
            knowledge_id=None,
            matched_by="RULE",
        )

    def _calculate_score(
        self,
        rule: IntentRule,
        normalized: str,
        compact: str,
        context: ConversationContext | None,
    ) -> float:
        score = 0.0

        for phrase in rule.strong_phrases:
            if self._contains(
                normalized,
                compact,
                phrase,
            ):
                score += self.STRONG_PHRASE_SCORE

        for keyword in rule.keywords:
            if self._contains(
                normalized,
                compact,
                keyword,
            ):
                score += self.KEYWORD_SCORE

        if self._can_use_context(
            rule=rule,
            normalized=normalized,
            compact=compact,
            context=context,
        ):
            score += self.CONTEXT_SCORE

        return score

    def _can_use_context(
        self,
        rule: IntentRule,
        normalized: str,
        compact: str,
        context: ConversationContext | None,
    ) -> bool:
        if context is None:
            return False

        if (
            rule.intent
            != Intent.CENTER_EARLIEST_AVAILABLE
        ):
            return False

        if (
            context.last_intent
            != Intent.CENTER_SEARCH.value
        ):
            return False

        if not context.last_keyword:
            return False

        return any(
            self._contains(
                normalized,
                compact,
                reference,
            )
            for reference in self.CONTEXT_REFERENCE_WORDS
        )

    def _resolve_context_keyword(
        self,
        intent: Intent,
        context: ConversationContext | None,
    ) -> str | None:
        if (
            intent
            != Intent.CENTER_EARLIEST_AVAILABLE
            or context is None
        ):
            return None

        return context.last_keyword

    @staticmethod
    def _contains(
        normalized: str,
        compact: str,
        search_text: str,
    ) -> bool:
        normalized_search = normalize_text(
            search_text
        )
        compact_search = compact_text(
            search_text
        )

        return (
            normalized_search in normalized
            or compact_search in compact
        )