package hongik.triple.apimodule.member;

import hongik.triple.apimodule.application.member.MemberService;
import hongik.triple.commonmodule.dto.member.MemberReq;
import hongik.triple.commonmodule.dto.member.MemberRes;
import hongik.triple.domainmodule.domain.member.Member;
import hongik.triple.domainmodule.domain.member.repository.MemberRepository;
import hongik.triple.inframodule.oauth.google.GoogleClient;
import hongik.triple.inframodule.oauth.google.GoogleProfile;
import hongik.triple.inframodule.oauth.google.GoogleToken;
import hongik.triple.inframodule.oauth.kakao.KakaoClient;
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

    @InjectMocks
    private MemberService memberService;

    @Nested
    @DisplayName("register()는")
    class RegisterTest {

        @Test
        @DisplayName("회원이 없을 경우 새로 등록한다.")
        void registerNewMember() {
            // given
            String email = "new@user.com";
            String name = "NewUser";
            GoogleToken googleToken = new GoogleToken(
                    "access_token",
                    "3600",
                    "Bearer",
                    "profile email",
                    "refresh_token",
                    "id_token"
            );
            GoogleProfile googleProfile = new GoogleProfile(
                    "sub_id",
                    name,
                    "given",
                    "family",
                    "profile.jpg",
                    email,
                    true,
                    "ko"
            );
            Member member = new Member(name, email);

            given(googleClient.getGoogleAccessToken(eq("access_token"), anyString()))
                    .willReturn(googleToken);
            given(googleClient.getMemberInfo(eq(googleToken)))
                    .willReturn(googleProfile);
            given(memberRepository.findByEmail(email))
                    .willReturn(Optional.empty());
            given(memberRepository.save(any(Member.class)))
                    .willReturn(member);

            // when
            MemberRes result = memberService.loginWithGoogle("access_token", "http://localhost/oauth2/callback/google");

            // then
            assertThat(result.email()).isEqualTo(email);
            assertThat(result.name()).isEqualTo(name);
        }

        @Test
        @DisplayName("회원이 이미 존재하면 기존 회원을 반환한다.")
        void registerExistingMember() {
            // given
            String email = "exist@user.com";
            String name = "Existing";
            Member existing = new Member(email, name); // ← 순서 확인 (email, name)
            GoogleToken googleToken = new GoogleToken(
                    "access_token",
                    "3600",
                    "Bearer",
                    "scope",
                    "refresh",
                    "id_token"
            );
            GoogleProfile googleProfile = new GoogleProfile(
                    "sub",
                    name,
                    "given",
                    "family",
                    "picture.jpg",
                    email,
                    true,
                    "ko"
            );

            given(googleClient.getGoogleAccessToken(eq("access_token"), anyString()))
                    .willReturn(googleToken);

            given(googleClient.getMemberInfo(eq(googleToken)))
                    .willReturn(googleProfile);

            given(memberRepository.findByEmail(email))
                    .willReturn(Optional.of(existing));

            // when
            MemberRes result = memberService.loginWithGoogle("access_token", "http://localhost/oauth2/callback/google");

            // then
            assertThat(result.email()).isEqualTo(existing.getEmail());
            assertThat(result.name()).isEqualTo(existing.getName());
        }

        @Test
        @DisplayName("Google 사용자 정보 조회 중 예외가 발생하면 예외를 던진다.")
        void getGoogleProfileThrowsException() {
            // given
            String authCode = "dummy_code";
            String redirectUri = "http://localhost/oauth2/callback/google";

            GoogleToken googleToken = new GoogleToken(
                    "dummy_access_token", "3600", "Bearer", "profile email", "dummy_refresh", "dummy_id_token"
            );

            // getGoogleAccessToken: redirectUri 정확히 일치시켜야 함
            given(googleClient.getGoogleAccessToken(eq(authCode), eq(redirectUri)))
                    .willReturn(googleToken);

            // getMemberInfo에서 예외 발생
            given(googleClient.getMemberInfo(eq(googleToken)))
                    .willThrow(new RuntimeException("Failed to call Google API"));

            // when & then
            assertThatThrownBy(() -> memberService.loginWithGoogle(authCode, redirectUri))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Google API");
        }

        @Test
        @DisplayName("카카오 사용자 정보 조회 중 예외가 발생하면 예외를 던진다.")
        void kakaoClientThrowsWhenGettingProfile() {
            // given
            String authCode = "dummy_auth_code";
            String redirectUri = "http://localhost/oauth2/callback/kakao";

            KakaoToken kakaoToken = KakaoToken.builder()
                    .access_token("dummy_access_token")
                    .refresh_token("dummy_refresh_token")
                    .token_type("bearer")
                    .expires_in(3600)
                    .refresh_token_expires_in(1209600)
                    .scope("profile account_email")
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
    @DisplayName("updateProfile()는")
    class UpdateProfileTest {

        @Test
        @DisplayName("정상적으로 회원 정보를 수정한다.")
        void success() {
            // given
            Member member = new Member("email@test.com", "old");
            MemberReq req = new MemberReq("new", "OILY");

            given(memberRepository.save(any(Member.class)))
                    .willReturn(member); // save 이후 리턴값 지정

            // when
            MemberRes res = memberService.updateProfile(member, req);

            // then
            assertThat(res.name()).isEqualTo("new");
        }
    }

    @Nested
    @DisplayName("getProfile()은")
    class GetProfileTest {

        @Test
        @DisplayName("회원 정보를 반환한다.")
        void success() {
            Member member = new Member("nick", "email@test.com");

            MemberRes res = memberService.getProfile(member);

            assertThat(res.email()).isEqualTo("email@test.com");
            assertThat(res.name()).isEqualTo("nick");
        }
    }

    @Nested
    @DisplayName("withdrawal()은")
    class WithdrawalTest {

        @Test
        @DisplayName("회원 탈퇴를 정상적으로 수행한다.")
        void success() {
            // given
            Member member = new Member("email@test.com", "nick");

            // when
            memberService.withdrawal(member);

            verify(memberRepository, times(1)).delete(member);
        }
    }
}