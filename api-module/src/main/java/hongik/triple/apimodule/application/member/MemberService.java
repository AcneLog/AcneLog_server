package hongik.triple.apimodule.application.member;

import hongik.triple.commonmodule.dto.member.MemberReq;
import hongik.triple.commonmodule.dto.member.MemberRes;
import hongik.triple.domainmodule.domain.member.Member;
import hongik.triple.domainmodule.domain.member.repository.MemberRepository;
import hongik.triple.inframodule.oauth.google.GoogleClient;
import hongik.triple.inframodule.oauth.google.GoogleProfile;
import hongik.triple.inframodule.oauth.google.GoogleToken;
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
    public void withdrawal(Member member) {
        memberRepository.delete(member);
    }

    public MemberRes loginWithKakao(String code, String redirectUri) {
        KakaoToken kakaoToken = kakaoClient.getKakaoAccessToken(code, redirectUri);
        KakaoProfile kakaoProfile = kakaoClient.getMemberInfo(kakaoToken);

        return register(kakaoProfile.kakao_account().email(), kakaoProfile.properties().nickname());
    }

    public MemberRes loginWithGoogle(String accessToken, String redirectUri) {
        GoogleToken googleToken = googleClient.getGoogleAccessToken(accessToken, redirectUri);
        GoogleProfile googleProfile = googleClient.getMemberInfo(googleToken);

        return register(googleProfile.email(), googleProfile.name());
    }

    public void logout() {

    }

    public MemberRes getProfile(Member member) {
        return MemberRes.builder()
                .id(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .build();
    }

    @Transactional
    public MemberRes updateProfile(Member member, MemberReq memberReq) {
        member.update(memberReq.name(), memberReq.skin_type());
        Member updateMember = memberRepository.save(member);

        return MemberRes.builder()
                .id(updateMember.getId())
                .email(updateMember.getEmail())
                .name(updateMember.getName())
                .build();
    }

    @Transactional
    protected MemberRes register(String email, String nickname) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (nickname == null || nickname.isEmpty()) {
            throw new IllegalArgumentException("Nickname cannot be null or empty");
        }

        return memberRepository.findByEmail(email)
                .map(member ->
                        MemberRes.builder()
                                .id(member.getId())
                                .email(member.getEmail())
                                .name(member.getName())
                                .build())
                .orElseGet(() -> {
                    Member newMember = new Member(email, nickname);
                    Member saveMember = memberRepository.save(newMember);
                    return MemberRes.builder()
                            .id(saveMember.getId())
                            .email(saveMember.getEmail())
                            .name(saveMember.getName())
                            .build();
                });
    }
}