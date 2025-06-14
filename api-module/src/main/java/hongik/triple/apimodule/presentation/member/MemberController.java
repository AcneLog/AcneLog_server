package hongik.triple.apimodule.presentation.member;

import hongik.triple.apimodule.application.member.MemberService;
import hongik.triple.apimodule.global.security.PrincipalDetails;
import hongik.triple.commonmodule.dto.member.MemberRes;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
@Tag(name = "Member", description = "회원 관련 API")
public class MemberController {

    private final MemberService memberService;

    /**
     * 로그인/회원가입 API - OAuth2 제공자에 따라 진행되며, 기존 로그인 정보 유무에 따라 회원가입 또는 로그인 처리
     * @param provider OAuth2 제공자 (예: kakao, google 등)
     * @return 회원 정보 응답 (MemberRes)
     */
    @PostMapping("/login")
    public MemberRes login(@RequestParam(name = "provider") String provider,
                           @RequestParam(name = "redirect-uri") String redirectUri) {
        // 회원가입 로직
        if(provider.equals("kakao")) {
            // 카카오 로그인 로직
            return memberService.loginWithKakao(provider, redirectUri);
        } else if(provider.equals("google")) {
            // 구글 로그인 로직
            return memberService.loginWithGoogle(provider, redirectUri);
        } else {
            throw new IllegalArgumentException("지원하지 않는 로그인 제공자입니다.");
        }
    }

    @PostMapping("/withdrawal")
    public void withdrawal(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        // 회원탈퇴 로직
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
