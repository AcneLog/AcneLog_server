package hongik.triple.apimodule.application.analysis;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisService {

     private final AIClient aiClient;
     private final YoutubeClient youtubeClient;
     private final NaverClient naverClient;
     private final AnalysisRepository analysisRepository;

     public AnalysisRes performAnalysis(Member member, MultipartFile multipartFile) {
         // Validation
         if(multipartFile.isEmpty() || multipartFile.getSize() == 0) {
             throw new IllegalArgumentException("File is empty");
         }

         // Business Logic
         // 피부 분석 AI 모델 호출
         AnalysisData analysisData = aiClient.sendPredictRequest(multipartFile);

         // 진단 결과를 기반으로 피부 관리 영상 추천 (유튜브 API)
         List<YoutubeVideoDto> videoList =
                 youtubeClient.searchVideos(analysisData.labelToSkinType().getDescription() + " 피부 관리", 3);

         // 진단 결과를 기반으로 맞춤형 제품 추천 (네이버 쇼핑 API)
         List<NaverProductDto> productList =
                 naverClient.searchProducts(analysisData.labelToSkinType().getDescription() + " 피부 관리", 3);
        
         // DB 저장
         Analysis analysis = Analysis.builder()
                 .member(member)
                 .acneType(analysisData.labelToSkinType())
                 .imageUrl("S3 URL or other storage URL")
                 .isPublic(true)
                 .videoData(videoList)
                 .productData(productList)
                 .build();
         Analysis saveAnalysis = analysisRepository.save(analysis);

         // Response
         return new AnalysisRes(
                 saveAnalysis.getAnalysisId(),
                 saveAnalysis.getImageUrl(),
                 saveAnalysis.getIsPublic(),
                 AcneType.valueOf(saveAnalysis.getAcneType()).name(),
                 AcneType.valueOf(saveAnalysis.getAcneType()).getDescription(),
                 AcneType.valueOf(saveAnalysis.getAcneType()).getCareMethod(),
                 AcneType.valueOf(saveAnalysis.getAcneType()).getGuide(),
                 videoList,
                 productList
         );
     }
}
