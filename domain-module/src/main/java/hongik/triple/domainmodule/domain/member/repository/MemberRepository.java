package hongik.triple.domainmodule.domain.member.repository;

import hongik.triple.domainmodule.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
}
