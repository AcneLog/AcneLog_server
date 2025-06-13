package hongik.triple.commonmodule.dto.member;

import lombok.Builder;

@Builder
public record MemberReq(
        String name,
        String skin_type
) {
}
