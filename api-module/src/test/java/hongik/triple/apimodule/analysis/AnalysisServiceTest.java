package hongik.triple.apimodule.analysis;

import hongik.triple.apimodule.application.analysis.AnalysisService;
import hongik.triple.commonmodule.dto.analysis.AnalysisData;
import hongik.triple.commonmodule.dto.analysis.AnalysisRes;
import hongik.triple.commonmodule.dto.analysis.NaverProductDto;
import hongik.triple.commonmodule.dto.analysis.YoutubeVideoDto;
import hongik.triple.commonmodule.enumerate.AcneType;
import hongik.triple.domainmodule.domain.analysis.Analysis;
import hongik.triple.domainmodule.domain.analysis.repository.AnalysisRepository;
import hongik.triple.domainmodule.domain.member.Member;
import hongik.triple.inframodule.ai.AIClient;
import hongik.triple.inframodule.naver.NaverClient;
import hongik.triple.inframodule.youtube.YoutubeClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AnalysisService 단위 테스트")
@ExtendWith(MockitoExtension.class)
public class AnalysisServiceTest {

    @InjectMocks
    private AnalysisService analysisService;

    @Mock
    private AIClient aiClient;

    @Mock
    private YoutubeClient youtubeClient;

    @Mock
    private NaverClient naverClient;

    @Mock
    private AnalysisRepository analysisRepository;

    @Mock
    private Member member;

    @Mock
    private MultipartFile multipartFile;

    private Analysis mockAnalysis;
    private AnalysisData mockAnalysisData;
    private List<YoutubeVideoDto> mockVideoList;
    private List<NaverProductDto> mockProductList;

    @BeforeEach
    void setUp() {
        // Member 설정
        lenient().when(member.getMemberId()).thenReturn(1L);

        // AnalysisData 설정
        mockAnalysisData = mock(AnalysisData.class);
        lenient().when(mockAnalysisData.labelToSkinType()).thenReturn(AcneType.COMEDONES);

        // YoutubeVideoDto 리스트 설정
        mockVideoList = List.of(
                new YoutubeVideoDto("video1", "여드름 피부 관리 방법", "여드름 관리 설명", "https://thumb1.jpg", "피부과 채널"),
                new YoutubeVideoDto("video2", "피부 타입별 관리법", "피부 관리 팁", "https://thumb2.jpg", "뷰티 채널"),
                new YoutubeVideoDto("video3", "좁쌀 여드름 케어", "좁쌀 관리법", "https://thumb3.jpg", "스킨케어 채널")
        );

        // NaverProductDto 리스트 설정 - 실제 생성자 파라미터 순서에 맞춤
        // (productId, productName, productUrl, productPrice, productImage, categoryName, mallName, brand)
        mockProductList = List.of(
                new NaverProductDto(
                        "product1",
                        "여드름 진정 크림",
                        "https://shopping.naver.com/product/1",
                        25000,
                        "https://image1.jpg",
                        "화장품/미용",
                        "올리브영",
                        "토리든"
                ),
                new NaverProductDto(
                        "product2",
                        "모공 케어 세럼",
                        "https://shopping.naver.com/product/2",
                        35000,
                        "https://image2.jpg",
                        "화장품/미용",
                        "올리브영",
                        "이니스프리"
                ),
                new NaverProductDto(
                        "product3",
                        "진정 토너",
                        "https://shopping.naver.com/product/3",
                        18000,
                        "https://image3.jpg",
                        "화장품/미용",
                        "올리브영",
                        "라운드랩"
                )
        );

        // Analysis 엔티티 설정
        mockAnalysis = mock(Analysis.class);
        lenient().when(mockAnalysis.getMember()).thenReturn(member);
        lenient().when(mockAnalysis.getAcneType()).thenReturn(String.valueOf(AcneType.COMEDONES));
        lenient().when(mockAnalysis.getImageUrl()).thenReturn("https://s3.test-image-url.jpg");
        lenient().when(mockAnalysis.getIsPublic()).thenReturn(true);
        lenient().when(mockAnalysis.getVideoData()).thenReturn(mockVideoList);
        lenient().when(mockAnalysis.getProductData()).thenReturn(mockProductList);
    }

    @Nested
    @DisplayName("performAnalysis 테스트")
    class PerformAnalysisTest {

        @Test
        @DisplayName("성공: 정상적인 피부 분석 수행")
        void performAnalysis_Success() {
            // Given
            when(multipartFile.isEmpty()).thenReturn(false);
            when(multipartFile.getSize()).thenReturn(1024L);
            when(aiClient.sendPredictRequest(multipartFile)).thenReturn(mockAnalysisData);
            when(youtubeClient.searchVideos(anyString(), eq(3))).thenReturn(mockVideoList);
            when(naverClient.searchProducts(anyString(), eq(3))).thenReturn(mockProductList);

            Analysis savedAnalysis = mock(Analysis.class);
            when(savedAnalysis.getAnalysisId()).thenReturn(1L);
            when(savedAnalysis.getImageUrl()).thenReturn("S3 URL or other storage URL");
            when(savedAnalysis.getIsPublic()).thenReturn(true);
            when(savedAnalysis.getAcneType()).thenReturn(String.valueOf(AcneType.COMEDONES));

            when(analysisRepository.save(ArgumentMatchers.any())).thenReturn(savedAnalysis);

            // When
            AnalysisRes result = analysisService.performAnalysis(member, multipartFile);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.analysisId());
            assertEquals("S3 URL or other storage URL", result.imageUrl());
            assertEquals(true, result.isPublic());
            assertEquals(AcneType.COMEDONES.name(), result.acneType());
            assertEquals(AcneType.COMEDONES.getDescription(), result.description());
            assertEquals(AcneType.COMEDONES.getCareMethod(), result.careMethod());
            assertEquals(AcneType.COMEDONES.getGuide(), result.guide());
            assertEquals(3, result.videoList().size());
            assertEquals(3, result.productList().size());

            verify(aiClient, times(1)).sendPredictRequest(multipartFile);
            verify(youtubeClient, times(1)).searchVideos(anyString(), eq(3));
            verify(naverClient, times(1)).searchProducts(anyString(), eq(3));
            verify(analysisRepository, times(1)).save(ArgumentMatchers.any());
        }

        @Test
        @DisplayName("실패: 빈 파일 업로드")
        void performAnalysis_Fail_EmptyFile() {
            // Given
            when(multipartFile.isEmpty()).thenReturn(true);

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> analysisService.performAnalysis(member, multipartFile)
            );

            assertEquals("File is empty", exception.getMessage());
            verify(aiClient, never()).sendPredictRequest(ArgumentMatchers.any());
            verify(analysisRepository, never()).save(ArgumentMatchers.any());
        }

        @Test
        @DisplayName("실패: 파일 크기가 0인 경우")
        void performAnalysis_Fail_ZeroSizeFile() {
            // Given
            when(multipartFile.isEmpty()).thenReturn(false);
            when(multipartFile.getSize()).thenReturn(0L);

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> analysisService.performAnalysis(member, multipartFile)
            );

            assertEquals("File is empty", exception.getMessage());
            verify(aiClient, never()).sendPredictRequest(ArgumentMatchers.any());
        }

        @Test
        @DisplayName("성공: 다양한 여드름 타입 처리 - NORMAL")
        void performAnalysis_Success_NormalType() {
            // Given
            AnalysisData normalData = mock(AnalysisData.class);
            when(normalData.labelToSkinType()).thenReturn(AcneType.NORMAL);

            when(multipartFile.isEmpty()).thenReturn(false);
            when(multipartFile.getSize()).thenReturn(1024L);
            when(aiClient.sendPredictRequest(multipartFile)).thenReturn(normalData);
            when(youtubeClient.searchVideos(anyString(), eq(3))).thenReturn(mockVideoList);
            when(naverClient.searchProducts(anyString(), eq(3))).thenReturn(mockProductList);

            Analysis savedAnalysis = mock(Analysis.class);
            when(savedAnalysis.getAnalysisId()).thenReturn(1L);
            when(savedAnalysis.getImageUrl()).thenReturn("test-url");
            when(savedAnalysis.getIsPublic()).thenReturn(true);
            when(savedAnalysis.getAcneType()).thenReturn(String.valueOf(AcneType.NORMAL));

            when(analysisRepository.save(ArgumentMatchers.any())).thenReturn(savedAnalysis);

            // When
            AnalysisRes result = analysisService.performAnalysis(member, multipartFile);

            // Then
            assertEquals(AcneType.NORMAL.name(), result.acneType());
            assertEquals(AcneType.NORMAL.getDescription(), result.description());
        }

        @Test
        @DisplayName("성공: 다양한 여드름 타입 처리 - PUSTULES")
        void performAnalysis_Success_PustulesType() {
            // Given
            AnalysisData pustulesData = mock(AnalysisData.class);
            when(pustulesData.labelToSkinType()).thenReturn(AcneType.PUSTULES);

            when(multipartFile.isEmpty()).thenReturn(false);
            when(multipartFile.getSize()).thenReturn(1024L);
            when(aiClient.sendPredictRequest(multipartFile)).thenReturn(pustulesData);
            when(youtubeClient.searchVideos(anyString(), eq(3))).thenReturn(mockVideoList);
            when(naverClient.searchProducts(anyString(), eq(3))).thenReturn(mockProductList);

            Analysis savedAnalysis = mock(Analysis.class);
            when(savedAnalysis.getAnalysisId()).thenReturn(1L);
            when(savedAnalysis.getImageUrl()).thenReturn("test-url");
            when(savedAnalysis.getIsPublic()).thenReturn(true);
            when(savedAnalysis.getAcneType()).thenReturn(String.valueOf(AcneType.PUSTULES));

            when(analysisRepository.save(ArgumentMatchers.any())).thenReturn(savedAnalysis);

            // When
            AnalysisRes result = analysisService.performAnalysis(member, multipartFile);

            // Then
            assertEquals(AcneType.PUSTULES.name(), result.acneType());
            assertEquals(AcneType.PUSTULES.getDescription(), result.description());
        }
    }

    @Nested
    @DisplayName("getAnalysisDetail 테스트")
    class GetAnalysisDetailTest {

        @Test
        @DisplayName("성공: 분석 상세 정보 조회")
        void getAnalysisDetail_Success() {
            // Given
            Long analysisId = 1L;
            when(mockAnalysis.getAnalysisId()).thenReturn(analysisId);
            when(analysisRepository.findById(analysisId)).thenReturn(Optional.of(mockAnalysis));

            // When
            AnalysisRes result = analysisService.getAnalysisDetail(member, analysisId);

            // Then
            assertNotNull(result);
            assertEquals(analysisId, result.analysisId());
            assertEquals("https://s3.test-image-url.jpg", result.imageUrl());
            assertEquals(true, result.isPublic());
            assertEquals(AcneType.COMEDONES.name(), result.acneType());
            assertEquals(AcneType.COMEDONES.getDescription(), result.description());
            assertEquals(AcneType.COMEDONES.getCareMethod(), result.careMethod());
            assertEquals(AcneType.COMEDONES.getGuide(), result.guide());
            assertEquals(3, result.videoList().size());
            assertEquals(3, result.productList().size());

            verify(analysisRepository, times(1)).findById(analysisId);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 분석 ID")
        void getAnalysisDetail_Fail_AnalysisNotFound() {
            // Given
            Long analysisId = 999L;
            when(analysisRepository.findById(analysisId)).thenReturn(Optional.empty());

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> analysisService.getAnalysisDetail(member, analysisId)
            );

            assertTrue(exception.getMessage().contains("Analysis not found"));
            verify(analysisRepository, times(1)).findById(analysisId);
        }

        @Test
        @DisplayName("실패: 권한 없는 사용자의 접근")
        void getAnalysisDetail_Fail_UnauthorizedAccess() {
            // Given
            Long analysisId = 1L;
            Member otherMember = mock(Member.class);
            when(otherMember.getMemberId()).thenReturn(2L);

            when(analysisRepository.findById(analysisId)).thenReturn(Optional.of(mockAnalysis));

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> analysisService.getAnalysisDetail(otherMember, analysisId)
            );

            assertTrue(exception.getMessage().contains("Unauthorized access"));
            verify(analysisRepository, times(1)).findById(analysisId);
        }
    }

    @Nested
    @DisplayName("getAnalysisListForMainPage 테스트")
    class GetAnalysisListForMainPageTest {

        @Test
        @DisplayName("성공: 메인 페이지용 최신 3개 분석 조회")
        void getAnalysisListForMainPage_Success() {
            // Given
            Analysis analysis1 = createMockAnalysis(1L, AcneType.COMEDONES);
            Analysis analysis2 = createMockAnalysis(2L, AcneType.PUSTULES);
            Analysis analysis3 = createMockAnalysis(3L, AcneType.PAPULES);

            List<Analysis> mockAnalyses = List.of(analysis1, analysis2, analysis3);
            when(analysisRepository.findTop3ByOrderByCreatedAtDesc()).thenReturn(mockAnalyses);

            // When
            List<AnalysisRes> result = analysisService.getAnalysisListForMainPage();

            // Then
            assertNotNull(result);
            assertEquals(3, result.size());
            assertEquals(AcneType.COMEDONES.name(), result.get(0).acneType());
            assertEquals(AcneType.PUSTULES.name(), result.get(1).acneType());
            assertEquals(AcneType.PAPULES.name(), result.get(2).acneType());
            verify(analysisRepository, times(1)).findTop3ByOrderByCreatedAtDesc();
        }

        @Test
        @DisplayName("성공: 분석 데이터가 3개 미만인 경우")
        void getAnalysisListForMainPage_Success_LessThanThree() {
            // Given
            Analysis analysis1 = createMockAnalysis(1L, AcneType.COMEDONES);
            List<Analysis> mockAnalyses = List.of(analysis1);
            when(analysisRepository.findTop3ByOrderByCreatedAtDesc()).thenReturn(mockAnalyses);

            // When
            List<AnalysisRes> result = analysisService.getAnalysisListForMainPage();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(analysisRepository, times(1)).findTop3ByOrderByCreatedAtDesc();
        }

        @Test
        @DisplayName("성공: 분석 데이터가 없는 경우")
        void getAnalysisListForMainPage_Success_EmptyList() {
            // Given
            when(analysisRepository.findTop3ByOrderByCreatedAtDesc()).thenReturn(List.of());

            // When
            List<AnalysisRes> result = analysisService.getAnalysisListForMainPage();

            // Then
            assertNotNull(result);
            assertEquals(0, result.size());
            verify(analysisRepository, times(1)).findTop3ByOrderByCreatedAtDesc();
        }
    }

    @Nested
    @DisplayName("getAnalysisPaginationForLogPage 테스트")
    class GetAnalysisPaginationForLogPageTest {

        private Pageable pageable;
        private Page<Analysis> mockPage;

        @BeforeEach
        void setUp() {
            pageable = PageRequest.of(0, 10);
            List<Analysis> analyses = List.of(
                    createMockAnalysis(1L, AcneType.COMEDONES),
                    createMockAnalysis(2L, AcneType.COMEDONES)
            );
            mockPage = new PageImpl<>(analyses, pageable, analyses.size());
        }

        @Test
        @DisplayName("성공: ALL 타입으로 전체 공개 분석 조회")
        void getAnalysisPaginationForLogPage_Success_AllType() {
            // Given
            when(analysisRepository.findByIsPublicTrueOrderByCreatedAtDesc(pageable))
                    .thenReturn(mockPage);

            // When
            Page<AnalysisRes> result = analysisService.getAnalysisPaginationForLogPage("ALL", pageable);

            // Then
            assertNotNull(result);
            assertEquals(2, result.getContent().size());
            verify(analysisRepository, times(1)).findByIsPublicTrueOrderByCreatedAtDesc(pageable);
            verify(analysisRepository, never()).findByIsPublicTrueAndAcneTypeOrderByCreatedAtDesc(anyString(), ArgumentMatchers.any());
        }

        @Test
        @DisplayName("성공: 특정 여드름 타입으로 공개 분석 조회")
        void getAnalysisPaginationForLogPage_Success_SpecificType() {
            // Given
            String acneType = "COMEDONES";
            when(analysisRepository.findByIsPublicTrueAndAcneTypeOrderByCreatedAtDesc(acneType, pageable))
                    .thenReturn(mockPage);

            // When
            Page<AnalysisRes> result = analysisService.getAnalysisPaginationForLogPage(acneType, pageable);

            // Then
            assertNotNull(result);
            assertEquals(2, result.getContent().size());
            verify(analysisRepository, times(1))
                    .findByIsPublicTrueAndAcneTypeOrderByCreatedAtDesc(acneType, pageable);
        }

        @Test
        @DisplayName("성공: 소문자 acne 타입 입력")
        void getAnalysisPaginationForLogPage_Success_LowerCase() {
            // Given
            String acneType = "comedones";
            when(analysisRepository.findByIsPublicTrueAndAcneTypeOrderByCreatedAtDesc("COMEDONES", pageable))
                    .thenReturn(mockPage);

            // When
            Page<AnalysisRes> result = analysisService.getAnalysisPaginationForLogPage(acneType, pageable);

            // Then
            assertNotNull(result);
            verify(analysisRepository, times(1))
                    .findByIsPublicTrueAndAcneTypeOrderByCreatedAtDesc("COMEDONES", pageable);
        }

        @Test
        @DisplayName("실패: 유효하지 않은 여드름 타입")
        void getAnalysisPaginationForLogPage_Fail_InvalidAcneType() {
            // Given
            String invalidType = "INVALID_TYPE";

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> analysisService.getAnalysisPaginationForLogPage(invalidType, pageable)
            );

            assertTrue(exception.getMessage().contains("Invalid acne type"));
            verify(analysisRepository, never()).findByIsPublicTrueOrderByCreatedAtDesc(ArgumentMatchers.any());
            verify(analysisRepository, never()).findByIsPublicTrueAndAcneTypeOrderByCreatedAtDesc(anyString(), ArgumentMatchers.any());
        }

        @Test
        @DisplayName("성공: 빈 페이지 반환")
        void getAnalysisPaginationForLogPage_Success_EmptyPage() {
            // Given
            Page<Analysis> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(analysisRepository.findByIsPublicTrueOrderByCreatedAtDesc(pageable))
                    .thenReturn(emptyPage);

            // When
            Page<AnalysisRes> result = analysisService.getAnalysisPaginationForLogPage("ALL", pageable);

            // Then
            assertNotNull(result);
            assertEquals(0, result.getContent().size());
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getAnalysisListForMyPage 테스트")
    class GetAnalysisListForMyPageTest {

        private Pageable pageable;
        private Page<Analysis> mockPage;

        @BeforeEach
        void setUp() {
            pageable = PageRequest.of(0, 10);
            List<Analysis> analyses = List.of(
                    createMockAnalysis(1L, AcneType.COMEDONES),
                    createMockAnalysis(2L, AcneType.PUSTULES)
            );
            mockPage = new PageImpl<>(analyses, pageable, analyses.size());
        }

        @Test
        @DisplayName("성공: ALL 타입으로 내 전체 분석 조회")
        void getAnalysisListForMyPage_Success_AllType() {
            // Given
            when(analysisRepository.findByMemberOrderByCreatedAtDesc(member, pageable))
                    .thenReturn(mockPage);

            // When
            Page<AnalysisRes> result = analysisService.getAnalysisListForMyPage(member, "ALL", pageable);

            // Then
            assertNotNull(result);
            assertEquals(2, result.getContent().size());
            verify(analysisRepository, times(1)).findByMemberOrderByCreatedAtDesc(member, pageable);
            verify(analysisRepository, never()).findByMemberAndAcneTypeOrderByCreatedAtDesc(ArgumentMatchers.any(), anyString(), ArgumentMatchers.any());
        }

        @Test
        @DisplayName("성공: 특정 여드름 타입으로 내 분석 조회")
        void getAnalysisListForMyPage_Success_SpecificType() {
            // Given
            String acneType = "COMEDONES";
            when(analysisRepository.findByMemberAndAcneTypeOrderByCreatedAtDesc(member, acneType, pageable))
                    .thenReturn(mockPage);

            // When
            Page<AnalysisRes> result = analysisService.getAnalysisListForMyPage(member, acneType, pageable);

            // Then
            assertNotNull(result);
            assertEquals(2, result.getContent().size());
            verify(analysisRepository, times(1))
                    .findByMemberAndAcneTypeOrderByCreatedAtDesc(member, acneType, pageable);
        }

        @Test
        @DisplayName("실패: Member가 null인 경우")
        void getAnalysisListForMyPage_Fail_NullMember() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> analysisService.getAnalysisListForMyPage(null, "ALL", pageable)
            );

            assertEquals("Member cannot be null", exception.getMessage());
            verify(analysisRepository, never()).findByMemberOrderByCreatedAtDesc(ArgumentMatchers.any(), ArgumentMatchers.any());
        }

        @Test
        @DisplayName("실패: 유효하지 않은 여드름 타입")
        void getAnalysisListForMyPage_Fail_InvalidAcneType() {
            // Given
            String invalidType = "INVALID_TYPE";

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> analysisService.getAnalysisListForMyPage(member, invalidType, pageable)
            );

            assertTrue(exception.getMessage().contains("Invalid acne type"));
            verify(analysisRepository, never()).findByMemberOrderByCreatedAtDesc(ArgumentMatchers.any(), ArgumentMatchers.any());
        }

        @Test
        @DisplayName("성공: 소문자 acne 타입 입력")
        void getAnalysisListForMyPage_Success_LowerCase() {
            // Given
            String acneType = "pustules";
            when(analysisRepository.findByMemberAndAcneTypeOrderByCreatedAtDesc(member, "PUSTULES", pageable))
                    .thenReturn(mockPage);

            // When
            Page<AnalysisRes> result = analysisService.getAnalysisListForMyPage(member, acneType, pageable);

            // Then
            assertNotNull(result);
            verify(analysisRepository, times(1))
                    .findByMemberAndAcneTypeOrderByCreatedAtDesc(member, "PUSTULES", pageable);
        }

        @Test
        @DisplayName("성공: 빈 페이지 반환")
        void getAnalysisListForMyPage_Success_EmptyPage() {
            // Given
            Page<Analysis> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(analysisRepository.findByMemberOrderByCreatedAtDesc(member, pageable))
                    .thenReturn(emptyPage);

            // When
            Page<AnalysisRes> result = analysisService.getAnalysisListForMyPage(member, "ALL", pageable);

            // Then
            assertNotNull(result);
            assertEquals(0, result.getContent().size());
            assertTrue(result.isEmpty());
        }
    }

    // Helper method
    private Analysis createMockAnalysis(Long id, AcneType acneType) {
        Analysis analysis = mock(Analysis.class);
        lenient().when(analysis.getAnalysisId()).thenReturn(id);
        lenient().when(analysis.getImageUrl()).thenReturn("https://s3.test-image-url-" + id + ".jpg");
        lenient().when(analysis.getIsPublic()).thenReturn(true);
        lenient().when(analysis.getAcneType()).thenReturn(String.valueOf(acneType));
        lenient().when(analysis.getVideoData()).thenReturn(mockVideoList);
        lenient().when(analysis.getProductData()).thenReturn(mockProductList);
        lenient().when(analysis.getMember()).thenReturn(member);
        return analysis;
    }
}
