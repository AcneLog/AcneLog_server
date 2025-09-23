package hongik.triple.commonmodule.dto.survey;

import java.util.List;

public record SurveyQuestionDto(
        String questionId,
        String questionText,
        String questionType, // MULTIPLE_CHOICE, SCALE, TEXT
        List<SurveyOptionDto> options,
        boolean required,
        int order
) {}