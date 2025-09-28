package hongik.triple.inframodule.ai;

import hongik.triple.commonmodule.dto.analysis.AnalysisData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class AIClient {

    private final WebClient webClient;

    /**
     * WebClient를 사용하여 FastAPI 서버와 통신
     *
     * @param webClientBuilder WebClient 빌더
     * @param baseUrl FastAPI 서버의 URL (application-infra.yml 에서 주입)
     */
    public AIClient(WebClient.Builder webClientBuilder,
                    @Value("${ai.server-url}") String baseUrl) {
        System.out.println("AIClient initialized with baseUrl: " + baseUrl);
        this.webClient = webClientBuilder
                .baseUrl(baseUrl) // 환경설정 값 사용
                .build();
    }

    /**
     * FastAPI 서버로 이미지 전송 후 예측 결과 받기
     *
     * @param file 업로드할 이미지 파일
     * @return FastAPI 모델의 JSON 응답
     */
    public AnalysisData sendPredictRequest(MultipartFile file) {
        return webClient.post()
                .uri("/predict")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("file", file.getResource()))
                .retrieve()
                .bodyToMono(AnalysisData.class)
                .block();
    }
}