package hongik.triple.apimodule.presentation.member;

import hongik.triple.apimodule.application.member.MemberService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
@Tag(name = "Member", description = "회원 관련 API")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/register")
    public void register() {
        // 회원가입 로직
    }

    @PostMapping("/withdrawal")
    public void withdrawal() {
        // 회원탈퇴 로직
    }

    @PostMapping("/login")
    public void login() {

    }

    @PostMapping("/logout")
    public void logout() {

    }

    @PostMapping("/profile")
    public void getProfile() {
        // 프로필 조회 로직
    }

    @PatchMapping("/update")
    public void updateProfile() {
        // 프로필 수정 로직
    }
}
