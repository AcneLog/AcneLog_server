package hongik.triple.apimodule.presentation.analysis;

import hongik.triple.apimodule.application.analysis.AnalysisService;
import hongik.triple.apimodule.global.common.ApplicationResponse;
import hongik.triple.apimodule.global.security.PrincipalDetails;
import hongik.triple.commonmodule.dto.analysis.AnalysisReq;
import hongik.triple.commonmodule.dto.analysis.AnalysisRes;
import hongik.triple.commonmodule.dto.survey.SurveyRes;
import hongik.triple.inframodule.s3.S3Client;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
@Tag(name = "Analysis", description = "피부 분석 관련 API")
public class AnalysisController {

    private final AnalysisService analysisService;
    private final S3Client s3Client;

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
    public ApplicationResponse<?> performAnalysis(@AuthenticationPrincipal PrincipalDetails principalDetails, @RequestPart(value = "file") MultipartFile multipartFile) {
        return ApplicationResponse.ok(analysisService.performAnalysis(principalDetails.getMember(), multipartFile));
    }

    @GetMapping("/main")
    @Operation(summary = "[홈화면] 피플즈 로그 썸네일 조회", description = "홈화면의 피플즈 로그에 노출되는 상위 3개의 분석 이미지를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "AnceLog Main Page 에서 노출할 피부 분석 이미지 결과 목록",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SurveyRes.class))),
            @ApiResponse(responseCode = "500",
                    description = "서버 오류")
    })
    public ApplicationResponse<?> getAnalysisListForMainPage() {
        return ApplicationResponse.ok(analysisService.getAnalysisListForMainPage());
    }

    @GetMapping("/my")
    @Operation(summary = "나의 진단로그 리스트 조회", description = "나의 진단로그 페이지의 리스트를 조회합니다.")
    public ApplicationResponse<?> getAnalysisListForMyPage(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                                           @RequestParam(name = "type") String acneType,
                                                           @PageableDefault(size = 4) Pageable pageable) {
        return ApplicationResponse.ok(analysisService.getAnalysisListForMyPage(principalDetails.getMember(), acneType, pageable));
    }

    @GetMapping("/detail/{analysisId}")
    @Operation(summary = "나의 진단로그 상세페이지 조회", description = "나의 진단로그 페이지의 상세 페이지를 조회합니다.")
    public ApplicationResponse<?> getAnalysisDetail(@AuthenticationPrincipal PrincipalDetails principalDetails, @PathVariable Long analysisId) {
        return ApplicationResponse.ok(analysisService.getAnalysisDetail(principalDetails.getMember(), analysisId));
    }

    @GetMapping("/log")
    @Operation(summary = "피플즈 로그 리스트 조회", description = "피플즈 로그 페이지의 리스트를 조회합니다.")
    public ApplicationResponse<?> getAnalysisPaginationForLogPage(@RequestParam(name = "type") String acneType,
                                                                  @PageableDefault(size = 4) Pageable pageable) {
        return ApplicationResponse.ok(analysisService.getAnalysisPaginationForLogPage(acneType, pageable));
    }

    @GetMapping("/log/{analysisId}")
    @Operation(summary = "피플즈 로그 상세페이지 조회", description = "피플즈 로그 페이지의 상세 페이지를 조회합니다.")
    public ApplicationResponse<?> getLogDetail(@PathVariable Long analysisId) {
        return ApplicationResponse.ok(analysisService.getLogDetail(analysisId));
    }

    @PatchMapping("/public")
    @Operation(summary = "진단 기록의 공개 여부 변경", description = "자신의 진단 기록에 대한 공개 여부를 변경합니다.")
    public ApplicationResponse<?> updateIsPublic(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody AnalysisReq req) {
        return ApplicationResponse.ok(analysisService.updateIsPublic(principalDetails.getMember(), req));
    }

    @PostMapping("/image")
    @Operation(summary = "이미지 업로드", description = "S3에 이미지를 업로드하는 API 입니다. (어드민용)")
    public ApplicationResponse<?> upload(@RequestPart MultipartFile file, @RequestParam(name = "dir") String dir) {

        return ApplicationResponse.ok(s3Client.uploadImage(file, dir));
    }

    @DeleteMapping("/image")
    @Operation(summary = "이미지 삭제", description = "S3에서 이미지를 삭제하는 API 입니다. (어드민용)")
    public ApplicationResponse<?> delete(@RequestParam String key) {

        s3Client.deleteImage(key);
        return ApplicationResponse.ok("이미지가 삭제되었습니다.");
    }

    @GetMapping("/main/youtube")
    @Operation(summary = "[홈화면] 유튜브 영상 추천 조회", description = "홈화면의 유튜브 영상 추천을 조회합니다.")
    public ApplicationResponse<?> getYoutubeRecommendationsForMainPage() {
        return ApplicationResponse.ok(analysisService.getYoutubeVideos());
    }

    @GetMapping("/main/product")
    @Operation(summary = "[홈화면] 상품 추천 조회", description = "홈화면의 상품 추천을 조회합니다.")
    public ApplicationResponse<?> getProductRecommendationsForMainPage() {
        return ApplicationResponse.ok(analysisService.getNaverProducts());
    }
}
