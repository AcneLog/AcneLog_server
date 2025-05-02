package hongik.triple.apimodule.application.member;

import hongik.triple.domainmodule.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    // 회원가입 로직
    public void register() {
        // Validation

        // Business Logic

        // Response
    }

}
