package hongik.triple.commonmodule.dto.survey;

import hongik.triple.commonmodule.enumerate.SkinType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Builder
public record SurveyRes(
        Long surveyId,
        Long memberId,
        String memberName,
        SkinType skinType,
        Map<String, Object> body,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        List<SurveyQuestionDto> questions,  // 설문 질문 조회용
        Integer totalScore,                  // 리스트 조회용
        String recommendation
) {}
