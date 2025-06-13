package hongik.triple.apimodule.presentation.member;

import hongik.triple.apimodule.application.member.MemberService;
import hongik.triple.commonmodule.dto.member.MemberRes;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
@Tag(name = "Member", description = "회원 관련 API")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/register")
    public MemberRes register(@RequestParam(name = "provider") String provider) {
        // 회원가입 로직
        if(provider.equals("kakao")) {
            // 카카오 로그인 로직
            return memberService.loginWithKakao(provider);
        } else if(provider.equals("google")) {
            // 구글 로그인 로직
            return memberService.loginWithGoogle(provider);
        } else {
            throw new IllegalArgumentException("지원하지 않는 로그인 제공자입니다.");
        }
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
