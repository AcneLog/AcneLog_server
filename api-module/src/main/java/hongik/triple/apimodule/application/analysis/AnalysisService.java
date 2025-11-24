package hongik.triple.apimodule.application.analysis;

import hongik.triple.commonmodule.dto.analysis.*;
import hongik.triple.commonmodule.enumerate.AcneType;
import hongik.triple.domainmodule.domain.analysis.Analysis;
import hongik.triple.domainmodule.domain.analysis.repository.AnalysisRepository;
import hongik.triple.domainmodule.domain.member.Member;
import hongik.triple.inframodule.ai.AIClient;
import hongik.triple.inframodule.naver.NaverClient;
import hongik.triple.inframodule.s3.S3Client;
import hongik.triple.inframodule.youtube.YoutubeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisService {

     private final AIClient aiClient;
     private final YoutubeClient youtubeClient;
     private final NaverClient naverClient;
     private final AnalysisRepository analysisRepository;
     private final S3Client s3Client;

     @Transactional
     public AnalysisRes performAnalysis(Member member, MultipartFile multipartFile) {
         // Validation
         if(multipartFile.isEmpty() || multipartFile.getSize() == 0) {
             throw new IllegalArgumentException("File is empty");
         }

         // Business Logic
         String s3_key = s3Client.uploadImage(multipartFile, "skin");

         // 피부 분석 AI 모델 호출
         AnalysisData analysisData = aiClient.sendPredictRequest(multipartFile);

         // 진단 결과를 기반으로 피부 관리 영상 추천 (유튜브 API)
         List<YoutubeVideoDto> videoList =
                 youtubeClient.searchVideos(analysisData.labelToSkinType().getKoreanName() + " 여드름", 3);

         // 진단 결과를 기반으로 맞춤형 제품 추천 (네이버 쇼핑 API)
         List<NaverProductDto> productList =
                 naverClient.searchProducts(analysisData.labelToSkinType().getKoreanName() + " 여드름", 3);

         // DB 저장
         Analysis analysis = Analysis.builder()
                 .member(member)
                 .acneType(analysisData.labelToSkinType())
                 .imageUrl(s3_key)
                 .isPublic(true)
                 .videoData(videoList)
                 .productData(productList)
                 .build();
         Analysis saveAnalysis = analysisRepository.save(analysis);

         // Response
         return new AnalysisRes(
                 saveAnalysis.getAnalysisId(),
                 s3Client.getImage(saveAnalysis.getImageUrl()),
                 formatted(saveAnalysis.getCreatedAt()),
                 saveAnalysis.getIsPublic(),
                 AcneType.valueOf(saveAnalysis.getAcneType()).name(),
                 AcneType.valueOf(saveAnalysis.getAcneType()).getDescription(),
                 AcneType.valueOf(saveAnalysis.getAcneType()).getCareMethod(),
                 AcneType.valueOf(saveAnalysis.getAcneType()).getGuide(),
                 videoList,
                 productList
         );
     }

     public AnalysisRes getAnalysisDetail(Member member, Long analysisId) {
         // Validation
         Analysis analysis = analysisRepository.findById(analysisId)
                 .orElseThrow(() -> new IllegalArgumentException("Analysis not found with id: " + analysisId));
         // Analysis가 요청한 사용자의 분석 결과인지 확인
         if(!analysis.getMember().getMemberId().equals(member.getMemberId())) {
             throw new IllegalArgumentException("Unauthorized access to analysis with id: " + analysisId);
         }

        // Response
         return new AnalysisRes(
                 analysis.getAnalysisId(),
                 s3Client.getImage(analysis.getImageUrl()),
                 formattedWithTime(analysis.getCreatedAt()),
                 analysis.getIsPublic(),
                 AcneType.valueOf(analysis.getAcneType()).name(),
                 AcneType.valueOf(analysis.getAcneType()).getDescription(),
                 AcneType.valueOf(analysis.getAcneType()).getCareMethod(),
                 AcneType.valueOf(analysis.getAcneType()).getGuide(),
                 analysis.getVideoData(),
                 analysis.getProductData()
         );
     }

     public MainLogRes getAnalysisListForMainPage() {
            // Business Logic
            List<Analysis> analyses = analysisRepository.findTop3ByIsPublicTrueOrderByCreatedAtDesc();
            int comedones = analysisRepository.countByAcneTypeAndIsPublicTrue("COMEDONES");
            int pustules = analysisRepository.countByAcneTypeAndIsPublicTrue("PUSTULES");
            int papules = analysisRepository.countByAcneTypeAndIsPublicTrue("PAPULES");
            int follicultis = analysisRepository.countByAcneTypeAndIsPublicTrue("FOLLICULITIS");

            // Response
            List<AnalysisRes> analysisList = analyses.stream().map(analysis -> new AnalysisRes(
                    analysis.getAnalysisId(),
                    s3Client.getImage(analysis.getImageUrl()),
                    formatted(analysis.getCreatedAt()),
                    analysis.getIsPublic(),
                    AcneType.valueOf(analysis.getAcneType()).name(),
                    AcneType.valueOf(analysis.getAcneType()).getDescription(),
                    AcneType.valueOf(analysis.getAcneType()).getCareMethod(),
                    AcneType.valueOf(analysis.getAcneType()).getGuide(),
                    analysis.getVideoData(),
                    analysis.getProductData()
            )).toList();

            return MainLogRes.from(comedones, pustules, papules, follicultis, analysisList);
     }

    /**
     * 피플즈 로그 페이지용 공개된 분석 기록 페이지네이션 조회
     * @param acneType 여드름 타입 (ALL인 경우 전체 조회)
     * @param pageable 페이지 정보
     * @return 페이지네이션된 공개 분석 기록 리스트
     */
    public Page<AnalysisRes> getAnalysisPaginationForLogPage(String acneType, Pageable pageable) {
        // Validation
        // acneType이 ALL이 아닌 경우 유효성 검증
        if (!"ALL".equalsIgnoreCase(acneType)) {
            try {
                AcneType.valueOf(acneType.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid acne type: " + acneType);
            }
        }

        // Business Logic
        Page<Analysis> analysisPage;

        // "ALL"인 경우 전체 공개 분석 조회, 아니면 타입별 공개 분석 조회
        if ("ALL".equalsIgnoreCase(acneType)) {
            analysisPage = analysisRepository.findByIsPublicTrueOrderByCreatedAtDesc(pageable);
        } else {
            analysisPage = analysisRepository.findByIsPublicTrueAndAcneTypeOrderByCreatedAtDesc(
                    acneType.toUpperCase(), pageable);
        }

        // Response
        return analysisPage.map(analysis -> new AnalysisRes(
                analysis.getAnalysisId(),
                s3Client.getImage(analysis.getImageUrl()),
                formatted(analysis.getCreatedAt()),
                analysis.getIsPublic(),
                AcneType.valueOf(analysis.getAcneType()).name(),
                AcneType.valueOf(analysis.getAcneType()).getDescription(),
                AcneType.valueOf(analysis.getAcneType()).getCareMethod(),
                AcneType.valueOf(analysis.getAcneType()).getGuide(),
                analysis.getVideoData(),
                analysis.getProductData()
        ));
    }

    /**
     * 마이페이지용 내 분석 기록 페이지네이션 조회
     * @param member 현재 로그인한 회원
     * @param acneType 여드름 타입 (ALL인 경우 전체 조회)
     * @param pageable 페이지 정보
     * @return 페이지네이션된 내 분석 기록 리스트
     */
    public Page<AnalysisRes> getAnalysisListForMyPage(Member member, String acneType, Pageable pageable) {
        // Validation
        if (member == null) {
            throw new IllegalArgumentException("Member cannot be null");
        }

        // acneType이 ALL이 아닌 경우 유효성 검증
        if (!"ALL".equalsIgnoreCase(acneType)) {
            try {
                AcneType.valueOf(acneType.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid acne type: " + acneType);
            }
        }

        // Business Logic
        Page<Analysis> analysisPage;

        // "ALL"인 경우 내 전체 분석 조회, 아니면 타입별 내 분석 조회
        if ("ALL".equalsIgnoreCase(acneType)) {
            analysisPage = analysisRepository.findByMemberOrderByCreatedAtDesc(member, pageable);
        } else {
            analysisPage = analysisRepository.findByMemberAndAcneTypeOrderByCreatedAtDesc(
                    member, acneType.toUpperCase(), pageable);
        }

        // Response
        return analysisPage.map(analysis -> new AnalysisRes(
                analysis.getAnalysisId(),
                s3Client.getImage(analysis.getImageUrl()),
                formatted(analysis.getCreatedAt()),
                analysis.getIsPublic(),
                AcneType.valueOf(analysis.getAcneType()).name(),
                AcneType.valueOf(analysis.getAcneType()).getDescription(),
                AcneType.valueOf(analysis.getAcneType()).getCareMethod(),
                AcneType.valueOf(analysis.getAcneType()).getGuide(),
                analysis.getVideoData(),
                analysis.getProductData()
        ));
    }

    /*
    피플즈 로그 개별 화면 조회
     */
    public AnalysisLogRes getLogDetail(Long analysisId) {
        // Validation
        Analysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new IllegalArgumentException("Analysis not found with id: " + analysisId));

        Member member = analysis.getMember();

        // Response
        return new AnalysisLogRes(
                analysis.getAnalysisId(),
                s3Client.getImage(analysis.getImageUrl()),
                member.getName(),
                member.getSkinType(),
                formattedWithTime(analysis.getCreatedAt()),
                analysis.getIsPublic(),
                AcneType.valueOf(analysis.getAcneType()).name(),
                AcneType.valueOf(analysis.getAcneType()).getDescription(),
                AcneType.valueOf(analysis.getAcneType()).getCareMethod(),
                AcneType.valueOf(analysis.getAcneType()).getGuide(),
                analysis.getVideoData(),
                analysis.getProductData()
        );
    }

    @Transactional
    public AnalysisRes updateIsPublic(Member member, AnalysisReq req) {
        // Validation
        Analysis analysis = analysisRepository.findById(req.analysisId())
                .orElseThrow(() -> new IllegalArgumentException("Analysis not found with id: " + req.analysisId()));
        // Analysis가 요청한 사용자의 분석 결과인지 확인
        if(!analysis.getMember().getMemberId().equals(member.getMemberId())) {
            throw new IllegalArgumentException("Unauthorized access to analysis with id: " + req.analysisId());
        }

        analysis.updateIsPublic(req.isPublic());

        return new AnalysisRes(
                analysis.getAnalysisId(),
                s3Client.getImage(analysis.getImageUrl()),
                formattedWithTime(analysis.getCreatedAt()),
                analysis.getIsPublic(),
                AcneType.valueOf(analysis.getAcneType()).name(),
                AcneType.valueOf(analysis.getAcneType()).getDescription(),
                AcneType.valueOf(analysis.getAcneType()).getCareMethod(),
                AcneType.valueOf(analysis.getAcneType()).getGuide(),
                analysis.getVideoData(),
                analysis.getProductData()
        );
    }

    private String formatted(LocalDateTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        return time.format(formatter);
    }

    private String formattedWithTime(LocalDateTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return time.format(formatter);
    }
}
