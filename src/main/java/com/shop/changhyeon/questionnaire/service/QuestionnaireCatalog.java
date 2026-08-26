package com.shop.changhyeon.questionnaire.service;

import com.shop.changhyeon.questionnaire.dto.QuestionnaireQuestionDto;

import java.util.List;
import java.util.Map;

final class QuestionnaireCatalog {

    private QuestionnaireCatalog() {
    }

    static final List<String> YES_NO = List.of("YES", "NO");

    static final List<QuestionnaireQuestionDto> QUESTIONS = List.of(
            question("feeling_well", "현재 건강", "오늘 몸 상태가 양호합니까?", "발열·심한 피로·호흡기 증상이 있다면 아니요를 선택하세요."),
            question("recent_donation", "헌혈 이력", "최근 헌혈 후 필요한 헌혈 간격이 지났습니까?", "전혈은 8주, 성분헌혈은 14일이 기본 기준입니다."),
            question("recent_treatment", "진료", "최근 수술·입원·수혈 또는 치료를 받은 적이 없습니까?", "해당 사항이 있으면 현장에서 의료진 확인이 필요합니다."),
            question("medication", "약물", "현재 복용 중이거나 최근 복용한 약이 없습니까?", "처방약·탈모약·여드름약 등을 포함합니다."),
            question("vaccination", "예방접종", "최근 예방접종을 받은 적이 없습니까?", "백신 종류에 따라 헌혈 가능 시점이 달라집니다."),
            question("infection", "질병", "최근 감염병 진단 또는 감염 의심 증상이 없습니까?", "완치 후에도 질병별 제한 기간이 적용될 수 있습니다."),
            question("dental_treatment", "진료", "최근 치과 치료를 받은 적이 없습니까?", "단순 검진을 제외한 치료 여부를 답해주세요."),
            question("pregnancy", "임신", "현재 임신 중이거나 출산·유산 후 6개월 이내가 아닙니까?", "해당하지 않는 경우 예를 선택하세요."),
            question("overseas_travel", "여행", "최근 4주 이내 해외여행을 하지 않았습니까?", "국가와 체류 지역에 따라 추가 제한이 있을 수 있습니다."),
            question("malaria_area", "여행", "최근 말라리아 관련 제한 지역에 거주하거나 숙박한 적이 없습니까?", "방문 지역과 기간을 의료진에게 알려주세요."),
            question("test_result", "검사 결과", "과거 헌혈검사에서 비정상 결과를 통보받은 적이 없습니까?", "B형·C형간염, 매독검사 등을 포함합니다."),
            question("risk_exposure", "감염 위험", "최근 혈액으로 전파될 수 있는 감염 위험에 노출된 적이 없습니까?", "정확한 판단은 현장 문진 담당자가 진행합니다.")
    );

    static final Map<String, String> SAFE_ANSWERS = Map.ofEntries(
            Map.entry("feeling_well", "YES"),
            Map.entry("recent_donation", "YES"),
            Map.entry("recent_treatment", "YES"),
            Map.entry("medication", "YES"),
            Map.entry("vaccination", "YES"),
            Map.entry("infection", "YES"),
            Map.entry("dental_treatment", "YES"),
            Map.entry("pregnancy", "YES"),
            Map.entry("overseas_travel", "YES"),
            Map.entry("malaria_area", "YES"),
            Map.entry("test_result", "YES"),
            Map.entry("risk_exposure", "YES")
    );

    private static QuestionnaireQuestionDto question(
            String id, String category, String text, String helpText
    ) {
        return new QuestionnaireQuestionDto(id, category, text, helpText, YES_NO);
    }
}
