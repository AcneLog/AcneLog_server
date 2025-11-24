package hongik.triple.commonmodule.dto.survey;

import java.util.List;

public record SurveyQuestionDto(
        String questionId,
        String questionText,
        String questionType,
        List<SurveyOptionDto> options,
        boolean required,
        int order,
        Integer selectedScore,      // 선택된 답변 점수 (nullable)
        String selectedOptionText   // 선택된 옵션 텍스트 (nullable)
) {
    // 질문만 있는 경우 (설문지 조회용)
    public SurveyQuestionDto(
            String questionId,
            String questionText,
            String questionType,
            List<SurveyOptionDto> options,
            boolean required,
            int order
    ) {
        this(questionId, questionText, questionType, options, required, order, null, null);
    }
}