package hongik.triple.apimodule.presentation.survey;

import hongik.triple.apimodule.application.survey.SurveyService;
import hongik.triple.apimodule.global.common.ApplicationResponse;
import hongik.triple.apimodule.global.security.PrincipalDetails;
import hongik.triple.commonmodule.dto.survey.SurveyReq;
import hongik.triple.commonmodule.dto.survey.SurveyRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/survey")
@RequiredArgsConstructor
@Tag(name = "Survey", description = "피부 타입 설문조사 API")
public class SurveyController {

    private final SurveyService surveyService;

    @GetMapping("/questions")
    @Operation(summary = "설문조사 질문 조회", description = "피부 타입 설문조사의 질문을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "설문조사 질문 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SurveyRes.class))),
            @ApiResponse(responseCode = "500",
                    description = "서버 오류")
    })
    public ApplicationResponse<?> getSurveyQuestions() {
        return ApplicationResponse.ok(surveyService.getSurveyQuestions());
    }

    @PostMapping("/response")
    @Operation(summary = "설문조사 제출", description = "사용자가 작성한 설문조사 응답을 제출합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "설문조사 제출 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SurveyRes.class))),
            @ApiResponse(responseCode = "400",
                    description = "잘못된 요청 (유효하지 않은 설문조사 데이터)"),
            @ApiResponse(responseCode = "500",
                    description = "서버 오류")
    })
    public ApplicationResponse<?> registerSurvey(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "설문조사 응답 데이터",
                    required = true,
                    content = @Content(schema = @Schema(implementation = SurveyReq.class)))
            @RequestBody SurveyReq request) {
        return ApplicationResponse.ok(surveyService.registerSurvey(principalDetails.getMember(), request));
    }

    @GetMapping("/list")
    @Operation(summary = "설문조사 결과 목록 조회", description = "사용자가 진행한 모든 설문조사 결과를 페이징하여 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "설문조사 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401",
                    description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "500",
                    description = "서버 오류")
    })
    public ApplicationResponse<?> getSurveyList(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Parameter(description = "페이징 정보 (page, size, sort)",
                    example = "page=0&size=10&sort=createdAt,desc")
            @PageableDefault Pageable pageable) {
        return ApplicationResponse.ok(surveyService.getSurveyList(principalDetails.getMember(), pageable)); // TODO: userDetails -> memberId 추출
    }

    @GetMapping("/detail/{surveyId}")
    @Operation(summary = "특정 설문조사 상세 조회", description = "설문조사 ID로 특정 설문조사의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "설문조사 상세 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SurveyRes.class))),
            @ApiResponse(responseCode = "404",
                    description = "존재하지 않는 설문조사"),
            @ApiResponse(responseCode = "500",
                    description = "서버 오류")
    })
    public ApplicationResponse<?> getSurveyDetail(
            @Parameter(description = "조회할 설문조사 ID", required = true, example = "1")
            @PathVariable(name = "surveyId") Long surveyId) {
        return ApplicationResponse.ok(surveyService.getSurveyDetail(surveyId));
    }
}
