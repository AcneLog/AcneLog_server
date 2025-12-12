package hongik.triple.apimodule.analysis;

import hongik.triple.apimodule.application.analysis.AnalysisService;
import hongik.triple.commonmodule.dto.analysis.*;
import hongik.triple.commonmodule.enumerate.AcneType;
import hongik.triple.commonmodule.enumerate.MemberType;
import hongik.triple.domainmodule.domain.analysis.Analysis;
import hongik.triple.domainmodule.domain.analysis.repository.AnalysisRepository;
import hongik.triple.domainmodule.domain.member.Member;
import hongik.triple.inframodule.ai.AIClient;
import hongik.triple.inframodule.naver.NaverClient;
import hongik.triple.inframodule.s3.S3Client;
import hongik.triple.inframodule.youtube.YoutubeClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalysisService 테스트")
class AnalysisServiceTest {

    @Mock
    private AIClient aiClient;

    @Mock
    private YoutubeClient youtubeClient;

    @Mock
    private NaverClient naverClient;

    @Mock
    private AnalysisRepository analysisRepository;

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private AnalysisService analysisService;

    private Member member;

    @BeforeEach
    void setup() {
        member = new Member("user", "email@test.com", MemberType.KAKAO);
        ReflectionTestUtils.setField(member, "memberId", 1L);
    }

    private MultipartFile mockFile() {
        return new MockMultipartFile("file", "test.jpg", "image/jpeg", "dummy".getBytes());
    }

    @Nested
    @DisplayName("performAnalysis()는")
    class PerformAnalysisTest {

        @Test
        @DisplayName("유효한 파일을 분석하고 결과를 반환한다.")
        void success() {
            MultipartFile file = mockFile();

            AnalysisData mockData = mock(AnalysisData.class);
            YoutubeVideoDto videoDto = new YoutubeVideoDto(
                    "id1",
                    "title1",
                    "url1",
                    "channel1",
                    "thumb1"
            );

            NaverProductDto productDto = new NaverProductDto(
                    "p1",
                    "상품1",
                    "url",
                    1000,
                    "img",
                    "category",
                    "mall",
                    "brand"
            );

            given(s3Client.uploadImage(file, "skin")).willReturn("s3/image.png");
            given(aiClient.sendPredictRequest(file)).willReturn(mockData);
            given(mockData.labelToSkinType()).willReturn(AcneType.PAPULES);

            given(youtubeClient.searchVideos(anyString(), eq(3)))
                    .willReturn(List.of(videoDto));
            given(naverClient.searchProducts(anyString(), eq(3)))
                    .willReturn(List.of(productDto));

            Analysis saved = Analysis.builder()
                    .member(member)
                    .acneType(AcneType.PAPULES)
                    .imageUrl("s3/image.png")
                    .isPublic(true)
                    .videoData(List.of(videoDto))
                    .productData(List.of(productDto))
                    .build();
            ReflectionTestUtils.setField(saved, "analysisId", 10L);
            ReflectionTestUtils.setField(saved, "createdAt", LocalDateTime.now());

            given(analysisRepository.save(any())).willReturn(saved);
            given(s3Client.getImage("s3/image.png")).willReturn("https://cdn/image.png");

            AnalysisRes res = analysisService.performAnalysis(member, file);

            assertThat(res.analysisId()).isEqualTo(10L);
            assertThat(res.acneType()).isEqualTo("PAPULES");
            assertThat(res.imageUrl()).isEqualTo("https://cdn/image.png");

            verify(analysisRepository, times(1)).save(any());
        }

        @Test
        @DisplayName("파일이 비어 있으면 예외를 던진다.")
        void emptyFile() {
            MultipartFile empty = new MockMultipartFile("file", new byte[]{});

            assertThatThrownBy(() -> analysisService.performAnalysis(member, empty))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("File is empty");

            verify(analysisRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getAnalysisDetail()은")
    class GetAnalysisDetailTest {

        @Test
        @DisplayName("자신의 분석 결과를 조회한다.")
        void success() {
            Analysis analysis = Analysis.builder()
                    .member(member)
                    .acneType(AcneType.COMEDONES)
                    .imageUrl("img.jpg")
                    .isPublic(true)
                    .videoData(List.of())
                    .productData(List.of())
                    .build();
            ReflectionTestUtils.setField(analysis, "analysisId", 20L);
            ReflectionTestUtils.setField(analysis, "createdAt", LocalDateTime.now());

            given(analysisRepository.findById(20L)).willReturn(Optional.of(analysis));
            given(s3Client.getImage("img.jpg")).willReturn("cdn/img.jpg");

            AnalysisRes res = analysisService.getAnalysisDetail(member, 20L);

            assertThat(res.analysisId()).isEqualTo(20L);
            assertThat(res.acneType()).isEqualTo("COMEDONES");
        }

        @Test
        @DisplayName("다른 사용자의 결과를 조회 시 예외 발생")
        void unauthorized() {
            Member another = new Member("other", "other@test.com", MemberType.GOOGLE);
            ReflectionTestUtils.setField(another, "memberId", 99L);

            Analysis analysis = Analysis.builder()
                    .member(another)
                    .acneType(AcneType.PAPULES)
                    .imageUrl("img")
                    .isPublic(true)
                    .videoData(List.of())
                    .productData(List.of())
                    .build();
            ReflectionTestUtils.setField(analysis, "analysisId", 20L);

            given(analysisRepository.findById(20L)).willReturn(Optional.of(analysis));

            assertThatThrownBy(() -> analysisService.getAnalysisDetail(member, 20L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unauthorized");
        }
    }

    @Nested
    @DisplayName("getAnalysisListForMainPage()는")
    class GetAnalysisListForMainPageTest {

        @Test
        @DisplayName("최신 공개된 분석 3개와 유형별 개수를 반환한다.")
        void success() {
            Analysis a1 = Analysis.builder()
                    .member(member)
                    .acneType(AcneType.COMEDONES)
                    .imageUrl("img1")
                    .isPublic(true)
                    .videoData(List.of())
                    .productData(List.of())
                    .build();

            ReflectionTestUtils.setField(a1, "analysisId", 1L);
            ReflectionTestUtils.setField(a1, "createdAt", LocalDateTime.now());

            given(analysisRepository.findTop3ByIsPublicTrueOrderByCreatedAtDesc())
                    .willReturn(List.of(a1));

            given(analysisRepository.countByAcneTypeAndIsPublicTrue("COMEDONES")).willReturn(3);
            given(analysisRepository.countByAcneTypeAndIsPublicTrue("PUSTULES")).willReturn(1);
            given(analysisRepository.countByAcneTypeAndIsPublicTrue("PAPULES")).willReturn(2);
            given(analysisRepository.countByAcneTypeAndIsPublicTrue("FOLLICULITIS")).willReturn(1);

            given(s3Client.getImage("img1")).willReturn("cdn/img1");

            MainLogRes res = analysisService.getAnalysisListForMainPage();

            assertThat(res.comedones()).isEqualTo(3);
            assertThat(res.analysisRes()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getAnalysisPaginationForLogPage()는")
    class GetPaginationForLogPageTest {

        @Test
        @DisplayName("ALL이면 전체 공개 분석을 조회한다.")
        void successAll() {
            Pageable pageable = PageRequest.of(0, 10);

            Analysis a = Analysis.builder()
                    .member(member)
                    .acneType(AcneType.PAPULES)
                    .imageUrl("img")
                    .isPublic(true)
                    .videoData(List.of())
                    .productData(List.of())
                    .build();

            ReflectionTestUtils.setField(a, "analysisId", 1L);
            ReflectionTestUtils.setField(a, "createdAt", LocalDateTime.now());

            Page<Analysis> page = new PageImpl<>(List.of(a));

            given(analysisRepository.findByIsPublicTrueOrderByCreatedAtDesc(pageable))
                    .willReturn(page);

            Page<AnalysisRes> res = analysisService.getAnalysisPaginationForLogPage("ALL", pageable);

            assertThat(res.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("특정 타입이면 타입별 공개 분석을 조회한다.")
        void successType() {
            Pageable pageable = PageRequest.of(0, 10);

            Analysis a = Analysis.builder()
                    .member(member)
                    .acneType(AcneType.PUSTULES)
                    .imageUrl("img")
                    .isPublic(true)
                    .videoData(List.of())
                    .productData(List.of())
                    .build();

            ReflectionTestUtils.setField(a, "analysisId", 1L);
            ReflectionTestUtils.setField(a, "createdAt", LocalDateTime.now());

            Page<Analysis> page = new PageImpl<>(List.of(a));

            given(analysisRepository.findByIsPublicTrueAndAcneTypeOrderByCreatedAtDesc("PUSTULES", pageable))
                    .willReturn(page);

            Page<AnalysisRes> res = analysisService.getAnalysisPaginationForLogPage("PUSTULES", pageable);

            assertThat(res.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("유효하지 않은 타입이면 예외 발생")
        void invalidType() {
            Pageable pageable = PageRequest.of(0, 10);

            assertThatThrownBy(() -> analysisService.getAnalysisPaginationForLogPage("INVALID", pageable))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid acne type");
        }
    }

    @Nested
    @DisplayName("getAnalysisListForMyPage()는")
    class GetListForMyPageTest {

        @Test
        @DisplayName("member가 null이면 예외 발생")
        void nullMember() {
            Pageable pageable = PageRequest.of(0, 10);
            assertThatThrownBy(() -> analysisService.getAnalysisListForMyPage(null, "ALL", pageable))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("ALL이면 내 전체 분석을 조회한다.")
        void successAll() {
            Pageable pageable = PageRequest.of(0, 10);

            Analysis a = Analysis.builder()
                    .member(member)
                    .acneType(AcneType.FOLLICULITIS)
                    .imageUrl("img")
                    .isPublic(true)
                    .videoData(List.of())
                    .productData(List.of())
                    .build();

            ReflectionTestUtils.setField(a, "analysisId", 1L);
            ReflectionTestUtils.setField(a, "createdAt", LocalDateTime.now());

            Page<Analysis> page = new PageImpl<>(List.of(a));

            given(analysisRepository.findByMemberOrderByCreatedAtDesc(member, pageable))
                    .willReturn(page);

            Page<AnalysisRes> res = analysisService.getAnalysisListForMyPage(member, "ALL", pageable);

            assertThat(res.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("타입별 조회 성공")
        void successType() {
            Pageable pageable = PageRequest.of(0, 10);

            Analysis a = Analysis.builder()
                    .member(member)
                    .acneType(AcneType.PUSTULES)
                    .imageUrl("img")
                    .videoData(List.of())
                    .productData(List.of())
                    .isPublic(true)
                    .build();

            ReflectionTestUtils.setField(a, "analysisId", 1L);
            ReflectionTestUtils.setField(a, "createdAt", LocalDateTime.now());

            Page<Analysis> page = new PageImpl<>(List.of(a));

            given(analysisRepository.findByMemberAndAcneTypeOrderByCreatedAtDesc(member, "PUSTULES", pageable))
                    .willReturn(page);

            Page<AnalysisRes> res = analysisService.getAnalysisListForMyPage(member, "PUSTULES", pageable);

            assertThat(res.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getLogDetail()은")
    class GetLogDetailTest {

        @Test
        @DisplayName("분석 로그 상세를 반환한다.")
        void success() {
            Analysis a = Analysis.builder()
                    .member(member)
                    .acneType(AcneType.COMEDONES)
                    .imageUrl("img")
                    .isPublic(true)
                    .videoData(List.of())
                    .productData(List.of())
                    .build();
            ReflectionTestUtils.setField(a, "analysisId", 40L);
            ReflectionTestUtils.setField(a, "createdAt", LocalDateTime.now());

            given(analysisRepository.findById(40L)).willReturn(Optional.of(a));
            given(s3Client.getImage("img")).willReturn("cdn/img");

            AnalysisLogRes res = analysisService.getLogDetail(40L);

            assertThat(res.analysisId()).isEqualTo(40L);
            assertThat(res.userName()).isEqualTo(member.getName());
        }

        @Test
        @DisplayName("존재하지 않는 로그 조회 시 예외 발생")
        void notFound() {
            given(analysisRepository.findById(123L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> analysisService.getLogDetail(123L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Analysis not found");
        }
    }

    @Nested
    @DisplayName("updateIsPublic()은")
    class UpdateIsPublicTest {

        @Test
        @DisplayName("공개 여부를 수정하고 반환한다.")
        void success() {
            AnalysisReq req = new AnalysisReq(10L, false);

            Analysis a = Analysis.builder()
                    .member(member)
                    .acneType(AcneType.PAPULES)
                    .imageUrl("img")
                    .isPublic(true)
                    .videoData(List.of())
                    .productData(List.of())
                    .build();
            ReflectionTestUtils.setField(a, "analysisId", 10L);
            ReflectionTestUtils.setField(a, "createdAt", LocalDateTime.now());

            given(analysisRepository.findById(10L)).willReturn(Optional.of(a));
            given(s3Client.getImage("img")).willReturn("cdn/img");

            AnalysisRes res = analysisService.updateIsPublic(member, req);

            assertThat(res.isPublic()).isFalse();
        }

        @Test
        @DisplayName("다른 사용자가 수정하면 예외 발생")
        void unauthorized() {
            Member attacker = new Member("attacker", "a@test.com", MemberType.GOOGLE);
            ReflectionTestUtils.setField(attacker, "memberId", 99L);

            AnalysisReq req = new AnalysisReq(10L, true);

            Analysis target = Analysis.builder()
                    .member(attacker)
                    .acneType(AcneType.PAPULES)
                    .imageUrl("img")
                    .isPublic(false)
                    .videoData(List.of())
                    .productData(List.of())
                    .build();
            ReflectionTestUtils.setField(target, "analysisId", 10L);

            given(analysisRepository.findById(10L)).willReturn(Optional.of(target));

            assertThatThrownBy(() -> analysisService.updateIsPublic(member, req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unauthorized");
        }
    }

    @Nested
    @DisplayName("getYoutubeVideos()는")
    class YoutubeVideosTest {

        @Test
        @DisplayName("피부관리 키워드로 영상 3개를 검색한다.")
        void success() {
            YoutubeVideoDto video = new YoutubeVideoDto(
                    "1",
                    "title",
                    "url",
                    "channel",
                    "thumb"
            );

            given(youtubeClient.searchVideos("피부관리", 3))
                    .willReturn(List.of(video));

            List<YoutubeVideoDto> res = analysisService.getYoutubeVideos();

            assertThat(res).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getNaverProducts()는")
    class NaverProductsTest {

        @Test
        @DisplayName("피부관리 키워드로 상품 3개 검색한다.")
        void success() {
            NaverProductDto product = new NaverProductDto(
                    "1",
                    "상품",
                    "url",
                    1000,
                    "img",
                    "category",
                    "mall",
                    "brand"
            );

            given(naverClient.searchProducts("피부관리", 3))
                    .willReturn(List.of(product));

            List<NaverProductDto> res = analysisService.getNaverProducts();

            assertThat(res).hasSize(1);
        }
    }
}

