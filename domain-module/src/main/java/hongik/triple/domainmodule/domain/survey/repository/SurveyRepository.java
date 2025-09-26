package hongik.triple.domainmodule.domain.survey.repository;

import hongik.triple.commonmodule.enumerate.SkinType;
import hongik.triple.domainmodule.domain.survey.Survey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SurveyRepository extends JpaRepository<Survey, Long> {

    Page<Survey> findByMember_MemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    Page<Survey> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Survey> findByMember_MemberIdAndSkinType(Long memberId, SkinType skinType);

    @Query("SELECT s FROM Survey s WHERE s.createdAt >= :startDate AND s.createdAt <= :endDate")
    List<Survey> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);
}
