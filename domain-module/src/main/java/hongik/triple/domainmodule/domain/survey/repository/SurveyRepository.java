package hongik.triple.domainmodule.domain.survey.repository;

import hongik.triple.domainmodule.domain.survey.Survey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyRepository extends JpaRepository<Survey, Long> {
}
