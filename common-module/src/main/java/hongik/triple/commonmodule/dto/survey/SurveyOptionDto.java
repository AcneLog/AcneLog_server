package hongik.triple.commonmodule.dto.survey;

public record SurveyOptionDto(
        String optionId,
        String optionText,
        int score
) {}