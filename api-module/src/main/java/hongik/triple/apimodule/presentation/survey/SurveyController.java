package hongik.triple.apimodule.presentation.survey;

import hongik.triple.apimodule.application.survey.SurveyService;
import hongik.triple.apimodule.global.common.ApplicationResponse;
import hongik.triple.commonmodule.dto.survey.SurveyRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ApplicationResponse<?> registerSurvey() {
        return ApplicationResponse.ok(surveyService.registerSurvey());
    }

    @GetMapping("/list")
    public ApplicationResponse<?> getSurveyList() {
        return ApplicationResponse.ok(surveyService.getSurveyList());
    }

    @GetMapping("/detail/{surveyId}")
    public ApplicationResponse<?> getSurveyDetail(Long surveyId) {
        return ApplicationResponse.ok(surveyService.getSurveyDetail(surveyId));
    }
}
