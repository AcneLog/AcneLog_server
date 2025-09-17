package hongik.triple.apimodule.application.survey;

import hongik.triple.commonmodule.dto.survey.SurveyRes;
import hongik.triple.domainmodule.domain.survey.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SurveyService {

    private final SurveyRepository surveyRepository;

    public SurveyRes registerSurvey() {
        // Validation

        // Business Logic

        // Response
        return new SurveyRes(); // Replace with actual response data
    }

    public SurveyRes getSurveyQuestions() {
        // Validation

        // Business Logic

        // Response
        return new SurveyRes(); // Replace with actual response data
    }

    public SurveyRes getSurveyList() {
        // Validation

        // Business Logic

        // Response
        return new SurveyRes(); // Replace with actual response data
    }

    public SurveyRes getSurveyDetail(Long surveyId) {
        // Validation

        // Business Logic

        // Response
        return new SurveyRes(); // Replace with actual response data
    }
}
