import re
import unicodedata


MULTIPLE_WHITESPACE_PATTERN = re.compile(r"\s+")
UNSUPPORTED_CHARACTER_PATTERN = re.compile(
    r"[^0-9a-z가-힣\s]"
)


NORMALIZATION_REPLACEMENTS = {
    "헌혈의 집": "헌혈의집",
    "헌혈 센터": "헌혈센터",
    "예약 내역": "예약내역",
    "예약 확인": "예약확인",
    "예약 조회": "예약조회",
    "헌혈 기록": "헌혈기록",
    "헌혈 내역": "헌혈내역",
    "검사 결과": "검사결과",
    "혈액 검사": "혈액검사",
    "전자 문진": "전자문진",
    "진위 확인": "진위확인",
    "혈액 사용 보류": "혈액사용보류",
    "보류 신청": "보류신청",
    "해두되나": "해도 되나",
    "해두돼": "해도 돼",
    "해두됨": "해도 됨",
    "해두되나요": "해도 되나요",
}


def normalize_text(text: str) -> str:
    normalized = unicodedata.normalize(
        "NFKC",
        text,
    ).lower()

    normalized = UNSUPPORTED_CHARACTER_PATTERN.sub(
        " ",
        normalized,
    )

    normalized = MULTIPLE_WHITESPACE_PATTERN.sub(
        " ",
        normalized,
    ).strip()

    for source, target in (
        NORMALIZATION_REPLACEMENTS.items()
    ):
        normalized = normalized.replace(
            source,
            target,
        )

    return normalized


def compact_text(text: str) -> str:
    return normalize_text(text).replace(
        " ",
        "",
    )