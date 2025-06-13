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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

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
            given(memberRepository.save(member)).willReturn(member);

            // when
            MemberRes result = memberService.loginWithGoogle("access_token");

            // then
            assertThat(result.email()).isEqualTo(email);
            assertThat(result.name()).isEqualTo(name);
        }

        @Test
        @DisplayName("회원이 이미 존재하면 기존 회원을 반환한다.")
        void registerExistingMember() {
            Member existing = new Member("exist@user.com", "Existing");
            when(memberRepository.findByEmail(existing.getEmail())).thenReturn(Optional.of(existing));

            MemberRes result = memberService.register(existing.getEmail(), existing.getName());

            assertThat(result.getEmail()).isEqualTo(existing.getEmail());
        }

        @Test
        @DisplayName("이메일이 null이면 예외 발생")
        void emailNullThrowsException() {
            assertThatThrownBy(() -> memberService.register(null, "nickname"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("닉네임이 빈 문자열이면 예외 발생")
        void nicknameEmptyThrowsException() {
            assertThatThrownBy(() -> memberService.register("test@email.com", ""))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("loginWithKakao()는")
    class LoginWithKakaoTest {

        @Test
        @DisplayName("정상적으로 로그인 후 회원 등록 또는 반환한다.")
        void success() {
            KakaoToken token = new KakaoToken("access");
            KakaoProfile profile = new KakaoProfile(new KakaoProfile.Account("email@test.com"), new KakaoProfile.Properties("nick"));

            when(kakaoClient.getKakaoAccessToken(any(), any())).thenReturn(token);
            when(kakaoClient.getMemberInfo(token)).thenReturn(profile);
            when(memberRepository.findByEmail("email@test.com")).thenReturn(Optional.empty());
            when(memberRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

            MemberRes res = memberService.loginWithKakao("code");

            assertThat(res.email()).isEqualTo("email@test.com");
            assertThat(res.name()).isEqualTo("nick");
        }
    }

    @Nested
    @DisplayName("loginWithGoogle()는")
    class LoginWithGoogleTest {

        @Test
        @DisplayName("정상적으로 로그인 후 회원 등록 또는 반환한다.")
        void success() {
            GoogleToken token = new GoogleToken("access");
            GoogleProfile profile = new GoogleProfile("sub", "name", "given", "family", "pic", "email@test.com", true, "ko");

            when(googleClient.getGoogleAccessToken(any(), any())).thenReturn(token);
            when(googleClient.getMemberInfo(token)).thenReturn(profile);
            when(memberRepository.findByEmail("email@test.com")).thenReturn(Optional.empty());
            when(memberRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

            MemberRes res = memberService.loginWithGoogle("access");

            assertThat(res.email()).isEqualTo("email@test.com");
            assertThat(res.name()).isEqualTo("name");
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
            Member member = new Member("email@test.com", "nick");

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
            Member member = new Member("email@test.com", "nick");

            memberService.withdrawal(member);

//            verify(memberRepository, times(1)).delete(member);
        }
    }
}