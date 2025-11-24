package hongik.triple.commonmodule.dto.survey;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SurveyRes(
        Long surveyId,
        Long memberId,
        String memberName,
        String skinType,
        Map<String, Object> body,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        List<SurveyQuestionDto> questions,  // 설문 질문 조회용
        Integer totalScore,                  // 리스트 조회용
        String recommendation
) {}
