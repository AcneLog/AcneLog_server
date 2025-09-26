package hongik.triple.commonmodule.dto.survey;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record SurveyReq(
        @NotNull(message = "회원 ID는 필수입니다.")
        Long memberId,

        @NotEmpty(message = "설문 응답은 필수입니다.")
        Map<String, Object> answers
) {}
