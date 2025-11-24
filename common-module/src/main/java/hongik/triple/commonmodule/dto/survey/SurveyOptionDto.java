package hongik.triple.commonmodule.dto.survey;

public record SurveyOptionDto(
        String optionId,
        String label,
        int value  // 점수 값
) {}