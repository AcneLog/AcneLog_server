package hongik.triple.domainmodule.domain.member.repository;

import hongik.triple.commonmodule.enumerate.MemberType;
import hongik.triple.domainmodule.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // 이메일로 회원 조회
    Optional<Member> findByEmail(String email);

    // 이메일과 회원 유형으로 회원 조회
    Optional<Member> findMemberByEmailAndMemberTypeAndDeletedAtIsNull(String email, MemberType memberType);
}
