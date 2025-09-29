package hongik.triple.inframodule.oauth.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleClient {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${spring.security.oauth2.client.registration.google.authorization-grant-type}")
    private String googleGrantType;

    @Value("${spring.security.oauth2.client.provider.google.token-uri}")
    private String googleTokenUri;

    @Value("${spring.security.oauth2.client.provider.google.user-info-uri}")
    private String googleUserInfoUri;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String googleRedirectUri;

    public String getGoogleAuthUrl(String redirectUri) {
        if(redirectUri == null || redirectUri.isEmpty()) {
            redirectUri = googleRedirectUri;
        }

        return "https://accounts.google.com/o/oauth2/v2/auth?client_id=" + googleClientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=code" +
                "&scope=email profile";
    }

    /**
     * 인가코드 기반으로 Google access token 발급
     */
    public GoogleToken getGoogleAccessToken(String code, String redirectUri) {
        // 별도의 리다이렉트 요청 URI 설정이 없을 경우, application.yml에 설정된 값 사용
        if (redirectUri == null || redirectUri.isEmpty()) {
            redirectUri = googleRedirectUri;
        }

        WebClient webClient = WebClient.create(googleTokenUri);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", googleGrantType);
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        String response = webClient.post()
                .uri(googleTokenUri)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(BodyInserters.fromFormData(params))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(response, GoogleToken.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Google token", e);
        }
    }

    /**
     * access token을 통해 Google 유저 정보 조회
     */
    public GoogleProfile getMemberInfo(GoogleToken googleToken) {
        WebClient webClient = WebClient.create(googleUserInfoUri);

        String response = webClient.get()
                .uri(googleUserInfoUri)
                .header("Authorization", "Bearer " + googleToken.access_token())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(response, GoogleProfile.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Google profile", e);
        }
    }
}