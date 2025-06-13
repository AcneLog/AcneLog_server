package hongik.triple.commonmodule.dto.member;

import lombok.Builder;

@Builder
public record MemberRes(
        Long id,
        String email,
        String name,
        String profileImageUrl,
        String thumbnailImageUrl,
        String nickname,
        String profileImagePath,
        String thumbnailImagePath
) {
}
