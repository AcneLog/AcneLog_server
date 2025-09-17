package hongik.triple.apimodule.application.analysis;

import hongik.triple.commonmodule.dto.analysis.AnalysisReq;
import hongik.triple.commonmodule.dto.analysis.AnalysisRes;
import hongik.triple.domainmodule.domain.analysis.repository.AnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisService {

     private final AnalysisRepository analysisRepository;

     public AnalysisRes performAnalysis(AnalysisReq request) {
         // Validation
         // Business Logic
         // Response
         return new AnalysisRes(); // Replace with actual response data
     }
}
