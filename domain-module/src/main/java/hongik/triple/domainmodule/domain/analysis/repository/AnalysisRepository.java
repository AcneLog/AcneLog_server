package hongik.triple.domainmodule.domain.analysis.repository;

import hongik.triple.domainmodule.domain.analysis.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {
}
