package hongik.triple.commonmodule.dto.member;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MemberRes(
        Long id,
        String email,
        String name,
        String skinType,
        String surveyTime,
        String profileImageUrl,
        String thumbnailImageUrl,
        String nickname,
        String profileImagePath,
        String thumbnailImagePath,
        String accessToken // JWT Access Token
) {
}
