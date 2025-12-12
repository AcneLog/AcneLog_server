package hongik.triple.apimodule.member;

import hongik.triple.apimodule.application.member.MemberService;
import hongik.triple.apimodule.global.security.jwt.TokenProvider;
import hongik.triple.apimodule.global.security.jwt.TokenDto;
import hongik.triple.commonmodule.dto.member.MemberReq;
import hongik.triple.commonmodule.dto.member.MemberRes;
import hongik.triple.commonmodule.enumerate.MemberType;
import hongik.triple.domainmodule.domain.member.Member;
import hongik.triple.domainmodule.domain.member.repository.MemberRepository;
import hongik.triple.inframodule.oauth.google.GoogleClient;
import hongik.triple.inframodule.oauth.google.GoogleProfile;
import hongik.triple.inframodule.oauth.google.GoogleToken;
import hongik.triple.inframodule.oauth.kakao.KakaoClient;
import hongik.triple.inframodule.oauth.kakao.KakaoProfile;
import hongik.triple.inframodule.oauth.kakao.KakaoToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DisplayName("MemberService 테스트")
@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private KakaoClient kakaoClient;

    @Mock
    private GoogleClient googleClient;

    @Mock
    private TokenProvider tokenProvider;

    @InjectMocks
    private MemberService memberService;

    @Nested
    @DisplayName("getKakaoLoginUrl()은")
    class GetKakaoLoginUrlTest {

        @Test
        @DisplayName("카카오 로그인 URL을 반환한다.")
        void success() {
            // given
            String redirectUri = "http://localhost:3000/auth/callback";
            String expectedUrl = "https://kauth.kakao.com/oauth/authorize?client_id=xxx&redirect_uri=" + redirectUri + "&response_type=code";

            given(kakaoClient.getKakaoAuthUrl(redirectUri))
                    .willReturn(expectedUrl);

            // when
            String result = memberService.getKakaoLoginUrl(redirectUri);

            // then
            assertThat(result).isEqualTo(expectedUrl);
            verify(kakaoClient, times(1)).getKakaoAuthUrl(redirectUri);
        }
    }

    @Nested
    @DisplayName("getGoogleLoginUrl()은")
    class GetGoogleLoginUrlTest {

        @Test
        @DisplayName("구글 로그인 URL을 반환한다.")
        void success() {
            // given
            String redirectUri = "http://localhost:3000/auth/callback";
            String expectedUrl = "https://accounts.google.com/o/oauth2/v2/auth?client_id=xxx&redirect_uri=" + redirectUri + "&response_type=code&scope=email profile";

            given(googleClient.getGoogleAuthUrl(redirectUri))
                    .willReturn(expectedUrl);

            // when
            String result = memberService.getGoogleLoginUrl(redirectUri);

            // then
            assertThat(result).isEqualTo(expectedUrl);
            verify(googleClient, times(1)).getGoogleAuthUrl(redirectUri);
        }
    }

    @Nested
    @DisplayName("loginWithKakao()는")
    class LoginWithKakaoTest {

        @Test
        @DisplayName("카카오 액세스 토큰과 사용자 정보를 정상적으로 조회한다.")
        void success() {
            // given
            String authCode = "kakao_auth_code";
            String redirectUri = "http://localhost:3000/auth/callback";

            KakaoToken kakaoToken = KakaoToken.builder()
                    .access_token("kakao_access_token")
                    .refresh_token("kakao_refresh_token")
                    .token_type("bearer")
                    .expires_in(3600)
                    .build();

            KakaoProfile.Properties properties = new KakaoProfile.Properties(
                    "카카오유저",
                    "https://example.com/profile.jpg",
                    "https://example.com/thumbnail.jpg"
            );

            KakaoProfile.KakaoAccount.Profile profile = new KakaoProfile.KakaoAccount.Profile(
                    "카카오유저",
                    "https://example.com/thumbnail.jpg",
                    "https://example.com/profile.jpg",
                    false,
                    false
            );

            KakaoProfile.KakaoAccount kakaoAccount = new KakaoProfile.KakaoAccount(
                    false,
                    false,
                    profile,
                    true,
                    false,
                    true,
                    true,
                    "kakao@test.com"
            );

            KakaoProfile kakaoProfile = new KakaoProfile(
                    12345L,
                    "2024-01-01T00:00:00Z",
                    properties,
                    kakaoAccount
            );

            given(kakaoClient.getKakaoAccessToken(authCode, redirectUri))
                    .willReturn(kakaoToken);
            given(kakaoClient.getMemberInfo(kakaoToken))
                    .willReturn(kakaoProfile);

            // when
            KakaoProfile result = memberService.loginWithKakao(authCode, redirectUri);

            // then
            assertThat(result.kakao_account().email()).isEqualTo("kakao@test.com");
            assertThat(result.properties().nickname()).isEqualTo("카카오유저");
            assertThat(result.kakao_account().profile().nickname()).isEqualTo("카카오유저");
            verify(kakaoClient, times(1)).getKakaoAccessToken(authCode, redirectUri);
            verify(kakaoClient, times(1)).getMemberInfo(kakaoToken);
        }

        @Test
        @DisplayName("카카오 사용자 정보 조회 중 예외가 발생하면 예외를 던진다.")
        void throwsExceptionWhenGettingProfile() {
            // given
            String authCode = "invalid_code";
            String redirectUri = "http://localhost:3000/auth/callback";

            KakaoToken kakaoToken = KakaoToken.builder()
                    .access_token("kakao_access_token")
                    .build();

            given(kakaoClient.getKakaoAccessToken(authCode, redirectUri))
                    .willReturn(kakaoToken);
            given(kakaoClient.getMemberInfo(kakaoToken))
                    .willThrow(new RuntimeException("카카오 사용자 정보 조회 실패"));

            // when & then
            assertThatThrownBy(() -> memberService.loginWithKakao(authCode, redirectUri))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("카카오 사용자 정보");
        }
    }

    @Nested
    @DisplayName("loginWithGoogle()는")
    class LoginWithGoogleTest {

        @Test
        @DisplayName("구글 액세스 토큰과 사용자 정보를 정상적으로 조회한다.")
        void success() {
            // given
            String authCode = "google_auth_code";
            String redirectUri = "http://localhost:3000/auth/callback";

            GoogleToken googleToken = new GoogleToken(
                    "google_access_token",
                    "3600",
                    "Bearer",
                    "profile email",
                    "google_refresh_token",
                    "id_token"
            );

            GoogleProfile googleProfile = new GoogleProfile(
                    "google_sub_id",
                    "구글유저",
                    "구글",
                    "유저",
                    "profile.jpg",
                    "google@test.com",
                    true,
                    "ko",
                    null
            );

            given(googleClient.getGoogleAccessToken(authCode, redirectUri))
                    .willReturn(googleToken);
            given(googleClient.getMemberInfo(googleToken))
                    .willReturn(googleProfile);

            // when
            GoogleProfile result = memberService.loginWithGoogle(authCode, redirectUri);

            // then
            assertThat(result.email()).isEqualTo("google@test.com");
            assertThat(result.name()).isEqualTo("구글유저");
            verify(googleClient, times(1)).getGoogleAccessToken(authCode, redirectUri);
            verify(googleClient, times(1)).getMemberInfo(googleToken);
        }

        @Test
        @DisplayName("구글 사용자 정보 조회 중 예외가 발생하면 예외를 던진다.")
        void throwsExceptionWhenGettingProfile() {
            // given
            String authCode = "invalid_code";
            String redirectUri = "http://localhost:3000/auth/callback";

            GoogleToken googleToken = new GoogleToken(
                    "google_access_token", "3600", "Bearer", "profile email", "refresh", "id_token"
            );

            given(googleClient.getGoogleAccessToken(authCode, redirectUri))
                    .willReturn(googleToken);
            given(googleClient.getMemberInfo(googleToken))
                    .willThrow(new RuntimeException("Failed to call Google API"));

            // when & then
            assertThatThrownBy(() -> memberService.loginWithGoogle(authCode, redirectUri))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Google API");
        }
    }

    @Nested
    @DisplayName("register()는")
    class RegisterTest {

        @Test
        @DisplayName("신규 회원을 등록하고 토큰을 발급한다.")
        void registerNewMember() {
            // given
            String email = "new@user.com";
            String nickname = "NewUser";
            MemberType memberType = MemberType.KAKAO;

            Member newMember = new Member(nickname, email, memberType);
            TokenDto tokenDto = new TokenDto("generated_access_token");

            given(memberRepository.findByEmail(email))
                    .willReturn(Optional.empty());
            given(memberRepository.save(any(Member.class)))
                    .willReturn(newMember);
            given(tokenProvider.createToken(any(Member.class)))
                    .willReturn(tokenDto);

            // when
            MemberRes result = memberService.register(email, nickname, memberType);

            // then
            assertThat(result.email()).isEqualTo(email);
            assertThat(result.name()).isEqualTo(nickname);
            assertThat(result.accessToken()).isEqualTo("generated_access_token");
            verify(memberRepository, times(1)).save(any(Member.class));
        }

        @Test
        @DisplayName("기존 회원이 존재하면 토큰만 재발급한다.")
        void registerExistingMember() {
            // given
            String email = "exist@user.com";
            String nickname = "ExistingUser";
            MemberType memberType = MemberType.GOOGLE;

            Member existingMember = new Member(nickname, email, memberType);
            TokenDto tokenDto = new TokenDto("new_access_token");

            given(memberRepository.findByEmail(email))
                    .willReturn(Optional.of(existingMember));
            given(tokenProvider.createToken(existingMember))
                    .willReturn(tokenDto);

            // when
            MemberRes result = memberService.register(email, nickname, memberType);

            // then
            assertThat(result.email()).isEqualTo(email);
            assertThat(result.name()).isEqualTo(nickname);
            assertThat(result.accessToken()).isEqualTo("new_access_token");
            verify(memberRepository, times(0)).save(any(Member.class)); // 저장 안 함
        }

        @Test
        @DisplayName("이메일이 null이면 예외를 던진다.")
        void throwsExceptionWhenEmailIsNull() {
            // when & then
            assertThatThrownBy(() -> memberService.register(null, "nickname", MemberType.KAKAO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Email");
        }

        @Test
        @DisplayName("닉네임이 비어있으면 예외를 던진다.")
        void throwsExceptionWhenNicknameIsEmpty() {
            // when & then
            assertThatThrownBy(() -> memberService.register("email@test.com", "", MemberType.KAKAO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Nickname");
        }
    }

    @Nested
    @DisplayName("getProfile()은")
    class GetProfileTest {

        @Test
        @DisplayName("회원 정보를 반환한다.")
        void success() {
            // given
            Member member = new Member("nickname", "email@test.com", MemberType.KAKAO);

            // when
            MemberRes result = memberService.getProfile(member);

            // then
            assertThat(result.email()).isEqualTo("email@test.com");
            assertThat(result.name()).isEqualTo("nickname");
        }
    }

    @Nested
    @DisplayName("updateProfile()은")
    class UpdateProfileTest {

        @Test
        @DisplayName("회원 정보를 정상적으로 수정한다.")
        void success() {
            // given
            Member member = new Member("oldName", "email@test.com", MemberType.KAKAO);
            MemberReq req = new MemberReq("newName", "OILY");

            given(memberRepository.save(any(Member.class)))
                    .willReturn(member);

            // when
            MemberRes result = memberService.updateProfile(member, req);

            // then
            assertThat(result.name()).isEqualTo("newName");
            assertThat(result.skinType()).isEqualTo("OILY");
            verify(memberRepository, times(1)).save(member);
        }
    }

    @Nested
    @DisplayName("withdrawal()은")
    class WithdrawalTest {

        @Test
        @DisplayName("회원 탈퇴를 정상적으로 수행한다.")
        void success() {
            // given
            Member member = new Member("nickname", "email@test.com", MemberType.GOOGLE);

            // when
            memberService.withdrawal(member);

            // then
            verify(memberRepository, times(1)).delete(member);
        }
    }
}