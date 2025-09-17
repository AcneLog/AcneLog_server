package hongik.triple.inframodule.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

@Component
public class AIClient {

    private final WebClient webClient;

    public AIClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://api.example.com") // AI 모델의 기본 URL 설정
                .build();
    }

    /**
     * AI 모델에 요청을 보내고 응답을 받는 메서드
     *
     * @param requestBody 요청 본문
     * @return AI 모델의 응답
     */
    public String sendRequest(String requestBody) {
        return webClient.post()
                .uri("/ai-endpoint") // AI 모델의 엔드포인트 URI
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block(); // 블로킹 방식으로 응답을 기다림
    }
}
