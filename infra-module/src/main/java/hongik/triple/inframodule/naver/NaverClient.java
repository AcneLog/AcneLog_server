package hongik.triple.inframodule.naver;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import hongik.triple.commonmodule.dto.analysis.NaverProductDto;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class NaverClient {
    // Reference: https://developers.naver.com/docs/serviceapi/search/shopping/shopping.md

    private final WebClient webClient;
    private final String clientId;
    private final String clientSecret;

    private static final String DOMAIN = "https://openapi.naver.com";
    private static final String SEARCH_PATH = "/v1/search/shop.json";

    public NaverClient(
            WebClient.Builder webClientBuilder,
            @Value("${naver.api.client-id}") String clientId,
            @Value("${naver.api.client-secret}") String clientSecret) {
        this.webClient = webClientBuilder.baseUrl(DOMAIN).build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    /**
     * 네이버 쇼핑 API - 상품 검색
     */
    public List<NaverProductDto> searchProducts(String keyword, int display) {
        try {
            NaverShoppingResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(SEARCH_PATH)
                            .queryParam("query", keyword)
                            .queryParam("display", display)
                            .queryParam("sort", "sim") // sim: 정확도순, date: 날짜순, asc/dsc: 가격 오름차순/내림차순
                            .build())
                    .header("X-Naver-Client-Id", clientId)
                    .header("X-Naver-Client-Secret", clientSecret)
                    .retrieve()
                    .bodyToMono(NaverShoppingResponse.class)
                    .block();

            if (response == null || response.items == null || response.items.isEmpty()) {
                log.warn("No products found for keyword: {}", keyword);
                return List.of();
            }

            return response.items.stream()
                    .map(item -> new NaverProductDto(
                            item.productId,
                            removeHtmlTags(item.title),
                            item.link,
                            item.lprice,
                            item.image,
                            item.category1,
                            item.mallName,
                            item.brand
                    ))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Failed to search Naver shopping products for keyword: {}", keyword, e);
            return List.of();
        }
    }

    /**
     * 진단명 기반 상품 추천
     */
    public List<NaverProductDto> getRecommendationsByDiagnosis(String diagnosisName) {
        String keyword = diagnosisName + " 피부 치료 크림";
        return searchProducts(keyword, 5);
    }

    /**
     * 피부 타입 기반 상품 추천
     */
    public List<NaverProductDto> getRecommendationsBySkinType(String skinType) {
        String keyword = skinType + " 피부 스킨케어";
        return searchProducts(keyword, 3);
    }

    /**
     * 가격대별 상품 검색
     */
    public List<NaverProductDto> searchProductsByPriceRange(String keyword, int minPrice, int maxPrice, int display) {
        try {
            NaverShoppingResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(SEARCH_PATH)
                            .queryParam("query", keyword)
                            .queryParam("display", display)
                            .queryParam("sort", "asc") // 가격 오름차순
                            .build())
                    .header("X-Naver-Client-Id", clientId)
                    .header("X-Naver-Client-Secret", clientSecret)
                    .retrieve()
                    .bodyToMono(NaverShoppingResponse.class)
                    .block();

            if (response == null || response.items == null || response.items.isEmpty()) {
                return List.of();
            }

            // 가격 필터링
            return response.items.stream()
                    .filter(item -> item.lprice >= minPrice && item.lprice <= maxPrice)
                    .map(item -> new NaverProductDto(
                            item.productId,
                            removeHtmlTags(item.title),
                            item.link,
                            item.lprice,
                            item.image,
                            item.category1,
                            item.mallName,
                            item.brand
                    ))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Failed to search products with price range: {} ~ {}", minPrice, maxPrice, e);
            return List.of();
        }
    }

    /**
     * HTML 태그 제거 (네이버 API는 제목에 <b> 태그를 포함하여 반환)
     */
    private String removeHtmlTags(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("<[^>]*>", "");
    }

    // Response DTOs
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NaverShoppingResponse {
        private String lastBuildDate;
        private int total;
        private int start;
        private int display;
        private List<NaverShoppingItem> items;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NaverShoppingItem {
        private String title;           // 상품명
        private String link;            // 상품 URL
        private String image;           // 상품 이미지 URL
        private int lprice;             // 최저가
        private int hprice;             // 최고가
        private String mallName;        // 쇼핑몰 이름
        private String productId;       // 상품 ID
        private String productType;     // 상품 타입
        private String brand;           // 브랜드
        private String maker;           // 제조사
        private String category1;       // 카테고리1
        private String category2;       // 카테고리2
        private String category3;       // 카테고리3
        private String category4;       // 카테고리4
    }
}