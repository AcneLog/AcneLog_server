package hongik.triple.inframodule.oauth.google;

public record GoogleToken(
        String access_token,
        String expires_in,
        String token_type,
        String scope,
        String refresh_token,
        String id_token
) {}
