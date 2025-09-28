package hongik.triple.apimodule.application.analysis;

import hongik.triple.commonmodule.dto.analysis.AnalysisData;
import hongik.triple.commonmodule.dto.analysis.AnalysisRes;
import hongik.triple.domainmodule.domain.analysis.Analysis;
import hongik.triple.domainmodule.domain.analysis.repository.AnalysisRepository;
import hongik.triple.domainmodule.domain.member.Member;
import hongik.triple.inframodule.ai.AIClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisService {

     private final AIClient aiClient;
     private final AnalysisRepository analysisRepository;

     public AnalysisRes performAnalysis(Member member, MultipartFile multipartFile) {
         // Validation
         if(multipartFile.isEmpty() || multipartFile.getSize() == 0) {
             throw new IllegalArgumentException("File is empty");
         }

         // Business Logic
         AnalysisData analysisData = aiClient.sendPredictRequest(multipartFile);
         System.out.println(analysisData);

//         Analysis.builder()
//                 .member(member)
//                 .skinType(analysisData.labelToSkinType())
//                 .build();

         // Response
         return new AnalysisRes(); // Replace with actual response data
     }
}
