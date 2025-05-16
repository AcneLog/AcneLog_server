package hongik.triple.apimodule.global.security.jwt;

import lombok.Builder;

@Builder
public record TokenDto(
        String accessToken
) {
}
