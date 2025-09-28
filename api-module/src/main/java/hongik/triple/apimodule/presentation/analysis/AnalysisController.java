package hongik.triple.apimodule.presentation.analysis;

import hongik.triple.apimodule.application.analysis.AnalysisService;
import hongik.triple.apimodule.global.common.ApplicationResponse;
import hongik.triple.apimodule.global.security.PrincipalDetails;
import hongik.triple.commonmodule.dto.analysis.AnalysisReq;
import hongik.triple.commonmodule.dto.analysis.AnalysisRes;
import hongik.triple.commonmodule.dto.survey.SurveyRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
@Tag(name = "Analysis", description = "피부 분석 관련 API")
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping("/perform")
    @Operation(summary = "피부 이미지 분석", description = "사용자에게 피부 이미지를 전달받아, 분석 결과를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "피부이미지 분석 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AnalysisRes.class))),
            @ApiResponse(responseCode = "500",
                    description = "서버 오류")
    })
    public ApplicationResponse<?> performAnalysis(@RequestPart(value = "file") MultipartFile multipartFile) {
        return ApplicationResponse.ok(analysisService.performAnalysis(null, multipartFile));
    }

    @GetMapping("/main")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "AnceLog Main Page 에서 노출할 피부 분석 이미지 결과 목록",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SurveyRes.class))),
            @ApiResponse(responseCode = "500",
                    description = "서버 오류")
    })
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
