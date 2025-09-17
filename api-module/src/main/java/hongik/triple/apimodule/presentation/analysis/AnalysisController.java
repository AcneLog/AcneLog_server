package hongik.triple.apimodule.presentation.analysis;

import hongik.triple.apimodule.application.analysis.AnalysisService;
import hongik.triple.apimodule.global.common.ApplicationResponse;
import hongik.triple.commonmodule.dto.analysis.AnalysisReq;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
@Tag(name = "Analysis", description = "피부 분석 관련 API")
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping("/perform")
    public ApplicationResponse<?> performAnalysis() {
        analysisService.performAnalysis(new AnalysisReq());
        return ApplicationResponse.ok();
    }

    // Polling for analysis results from AI model worker
    @GetMapping("/poll")
    public ApplicationResponse<?> pollAnalysis() {
        return ApplicationResponse.ok();
    }

    @GetMapping("/main")
    public ApplicationResponse<?> getAnalysisListForMainPage() {
        return ApplicationResponse.ok();
    }

    @GetMapping("/my")
    public ApplicationResponse<?> getAnalysisListForMyPage() {
        return ApplicationResponse.ok();
    }

    @GetMapping("/detail/{analysisId}")
    public ApplicationResponse<?> getAnalysisDetail(@PathVariable Long analysisId) {
        return ApplicationResponse.ok();
    }

    @GetMapping("/log")
    public ApplicationResponse<?> getAnalysisPaginationForLogPage() {
        return ApplicationResponse.ok();
    }
}
