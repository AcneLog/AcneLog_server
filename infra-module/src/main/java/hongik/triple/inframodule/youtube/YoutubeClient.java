package hongik.triple.inframodule.youtube;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import hongik.triple.commonmodule.dto.analysis.YoutubeVideoDto;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class YoutubeClient {

    private final WebClient webClient;
    private final String apiKey;

    public YoutubeClient(
            WebClient.Builder webClientBuilder,
            @Value("${youtube.api.key}") String apiKey,
            @Value("${youtube.api.base-url}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    /**
     * 키워드로 YouTube 영상 검색
     */
    public List<YoutubeVideoDto> searchVideos(String query, int maxResults) {
        try {
            log.info("YouTube 검색 시작 - query: {}, maxResults: {}", query, maxResults);

            YoutubeSearchResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("part", "snippet")
                            .queryParam("q", query)
                            .queryParam("type", "video")
                            .queryParam("maxResults", maxResults)
                            .queryParam("key", apiKey)
                            // .queryParam("order", "relevance")  // 기본값이 relevance라 생략 가능
                            // .queryParam("regionCode", "KR")  // 선택 사항
                            // .queryParam("relevanceLanguage", "ko")  // 선택 사항
                            .build())
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .doOnNext(errorBody ->
                                            log.error("YouTube API 에러 응답: {}", errorBody))
                                    .then(Mono.error(new RuntimeException("YouTube API 호출 실패")))
                    )
                    .bodyToMono(YoutubeSearchResponse.class)
                    .block();

            if (response == null || response.items == null) {
                log.warn("YouTube API 응답이 비어있습니다.");
                return List.of();
            }

            log.info("YouTube 검색 성공 - {} 개의 결과 반환", response.items.size());

            return response.items.stream()
                    .map(item -> new YoutubeVideoDto(
                            item.id.videoId,
                            item.snippet.title,
                            "https://www.youtube.com/watch?v=" + item.id.videoId,
                            item.snippet.channelTitle,
                            item.snippet.thumbnails.high != null
                                    ? item.snippet.thumbnails.high.url
                                    : (item.snippet.thumbnails.defaultThumbnail != null
                                    ? item.snippet.thumbnails.defaultThumbnail.url
                                    : "")
                    ))
                    .collect(Collectors.toList());

        } catch (WebClientResponseException e) {
            log.error("YouTube API 호출 실패 - Status: {}, Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            return List.of();
        } catch (Exception e) {
            log.error("YouTube 검색 중 예외 발생 - query: {}", query, e);
            return List.of();
        }
    }

    /**
     * 진단명 기반 영상 추천
     */
    public List<YoutubeVideoDto> getRecommendationsByDiagnosis(String diagnosisName) {
        String query = diagnosisName + " 피부 관리";  // "치료" 제거 (더 넓은 검색)
        return searchVideos(query, 5);
    }

    /**
     * 피부 타입 기반 영상 추천
     */
    public List<YoutubeVideoDto> getRecommendationsBySkinType(String skinType) {
        String query = skinType + " 피부 관리";  // "스킨케어" 제거 (더 넓은 검색)
        return searchVideos(query, 3);
    }

    // Response DTOs
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class YoutubeSearchResponse {
        private List<YoutubeSearchItem> items;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class YoutubeSearchItem {
        private VideoId id;
        private VideoSnippet snippet;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VideoId {
        private String videoId;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VideoSnippet {
        private String title;
        private String channelTitle;
        private Thumbnails thumbnails;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Thumbnails {
        @JsonProperty("high")
        private Thumbnail high;
        @JsonProperty("default")
        private Thumbnail defaultThumbnail;
        @JsonProperty("medium")
        private Thumbnail medium;  // 추가: fallback 옵션
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Thumbnail {
        private String url;
    }
}