package hongik.triple.apimodule.presentation.survey;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/survey")
@RequiredArgsConstructor
@Tag(name = "Survey", description = "피부 타입 설문조사 API")
public class SurveyController {
}
