package hongik.triple.domainmodule.domain.analysis.repository;

import hongik.triple.domainmodule.domain.analysis.Analysis;
import hongik.triple.domainmodule.domain.member.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

    // 메인 페이지용
    List<Analysis> findTop3ByIsPublicTrueOrderByCreatedAtDesc();
    int countByAcneTypeAndIsPublicTrue(String acneType);

    // 피플즈 로그 페이지용 - 전체 공개 분석 조회
    Page<Analysis> findByIsPublicTrueOrderByCreatedAtDesc(Pageable pageable);

    // 피플즈 로그 페이지용 - 타입별 공개 분석 조회
    Page<Analysis> findByIsPublicTrueAndAcneTypeOrderByCreatedAtDesc(String acneType, Pageable pageable);

    // 마이페이지용 - 내 전체 분석 조회
    Page<Analysis> findByMemberOrderByCreatedAtDesc(Member member, Pageable pageable);

    // 마이페이지용 - 내 타입별 분석 조회
    Page<Analysis> findByMemberAndAcneTypeOrderByCreatedAtDesc(Member member, String acneType, Pageable pageable);
}
