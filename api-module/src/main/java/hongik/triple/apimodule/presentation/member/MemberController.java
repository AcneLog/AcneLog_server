package hongik.triple.apimodule.presentation.member;

import hongik.triple.apimodule.application.member.MemberService;
import hongik.triple.apimodule.global.common.ApplicationResponse;
import hongik.triple.apimodule.global.security.PrincipalDetails;
import hongik.triple.commonmodule.dto.member.MemberReq;
import hongik.triple.commonmodule.dto.member.MemberRes;
import hongik.triple.commonmodule.enumerate.MemberType;
import hongik.triple.inframodule.oauth.google.GoogleProfile;
import hongik.triple.inframodule.oauth.kakao.KakaoProfile;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Member", description = "회원 관련 API")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/auth/login")
    public ResponseEntity<?> redirectLoginPage(
            @RequestParam(name = "provider") String provider,
            @RequestParam(name = "redirect-uri", required = false) String redirectUri) {
        String authUrl = switch (provider) {
            case "kakao" -> memberService.getKakaoLoginUrl(redirectUri);
            case "google" -> memberService.getGoogleLoginUrl(redirectUri);
            default -> throw new IllegalArgumentException("지원하지 않는 로그인 제공자입니다.");
        };

        if(redirectUri == null || redirectUri.isEmpty()) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", authUrl);
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        } else {
            return ResponseEntity.ok(ApplicationResponse.ok(authUrl));
        }
    }

    /**
     * 카카오 로그인/회원가입 API - 기존 로그인 정보 유무에 따라 회원가입 또는 로그인 처리
     * @return 회원 정보 응답 (MemberRes)
     */
    @GetMapping("/auth/kakao/login")
    public ApplicationResponse<MemberRes> loginWithKakao(
            @RequestParam(name = "code") String authorizationCode,
            @RequestParam(name = "redirect-uri", required = false) String redirectUri) {
        KakaoProfile kakaoProfile = memberService.loginWithKakao(authorizationCode, redirectUri);
        return ApplicationResponse.ok(memberService.register(kakaoProfile.kakao_account().email(), kakaoProfile.properties().nickname(), MemberType.KAKAO));
    }

    /**
     * 구글 로그인/회원가입 API - 기존 로그인 정보 유무에 따라 회원가입 또는 로그인 처리
     * @return 회원 정보 응답 (MemberRes)
     */
    @GetMapping("/auth/google/login")
    public ApplicationResponse<MemberRes> loginWithGoogle(
            @RequestParam(name = "code") String authorizationCode,
            @RequestParam(name = "redirect-uri", required = false) String redirectUri) {
        GoogleProfile googleProfile = memberService.loginWithGoogle(authorizationCode, redirectUri);
        return ApplicationResponse.ok(memberService.register(googleProfile.email(), googleProfile.name(), MemberType.GOOGLE));
    }

    @PostMapping("/member/withdrawal")
    public void withdrawal(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        // 회원탈퇴 로직
        memberService.withdrawal(principalDetails.getMember());
    }

    @GetMapping("/member/profile")
    public ApplicationResponse<MemberRes> getProfile(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ApplicationResponse.ok(memberService.getProfile(principalDetails.getMember()));
    }

    @PatchMapping("/member/update")
    public void updateProfile(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody MemberReq req) {
        memberService.updateProfile(principalDetails.getMember(), req);
    }
}
