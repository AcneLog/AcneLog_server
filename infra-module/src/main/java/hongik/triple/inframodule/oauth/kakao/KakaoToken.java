package hongik.triple.inframodule.oauth.kakao;

import lombok.Builder;

@Builder
public record KakaoToken(
        String access_token,
        String refresh_token,
        String token_type,
        Integer expires_in,
        Integer refresh_token_expires_in,
        String scope
) {
}
