package hongik.triple.apimodule.application.member;

import hongik.triple.commonmodule.dto.member.MemberRes;
import hongik.triple.domainmodule.domain.member.repository.MemberRepository;
import hongik.triple.inframodule.oauth.google.GoogleClient;
import hongik.triple.inframodule.oauth.kakao.KakaoClient;
import hongik.triple.inframodule.oauth.kakao.KakaoProfile;
import hongik.triple.inframodule.oauth.kakao.KakaoToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final KakaoClient kakaoClient;
    private final GoogleClient googleClient;

    @Transactional
    public void withdrawal() {
        // 회원탈퇴 로직 구현
    }

    public MemberRes loginWithKakao(String accessToken) {
        // Kakao 로그인 로직
        KakaoToken kakaoToken = kakaoClient.getKakaoAccessToken(accessToken, "redirectUri");
        KakaoProfile kakaoProfile = kakaoClient.getMemberInfo(kakaoToken);
        // 추가 비즈니스 로직

        // Response
        return null;
    }

    public MemberRes loginWithGoogle(String accessToken) {
        // Google 로그인 로직

        // 추가 비즈니스 로직

        // Response
        return null;
    }

    public void logout() {
        // 로그아웃 로직 구현
    }

    public void getProfile() {
        // 프로필 조회 로직 구현
    }

    @Transactional
    public void updateProfile() {
        // 프로필 수정 로직 구현
    }

    /**
     * 회원가입 로직을 구현합니다. 구글 로그인 또는 카카오 로그인을 통해 가져온 정보를 기반으로 회원가입을 진행합니다.
     */
    private void register() {
        // 회원가입 로직 구현
    }
}
