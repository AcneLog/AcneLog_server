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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final KakaoClient kakaoClient;
    private final GoogleClient googleClient;

    public String getKakaoLoginUrl(String redirectUri) {
        return kakaoClient.getKakaoAuthUrl(redirectUri);
    }

    public String getGoogleLoginUrl(String redirectUri) {
        return ""; // TODO: 추후 구현 예정
    }

    public KakaoProfile loginWithKakao(String authorizationCode) {
        KakaoToken kakaoToken = kakaoClient.getKakaoAccessToken(authorizationCode);
        return kakaoClient.getMemberInfo(kakaoToken);
    }

    public MemberRes loginWithGoogle(String authorizationCode) {
        GoogleToken googleToken = googleClient.getGoogleAccessToken(authorizationCode);
        GoogleProfile googleProfile = googleClient.getMemberInfo(googleToken);

        return register(googleProfile.email(), googleProfile.name());
    }

    @Transactional
    public void withdrawal(Member member) {
        memberRepository.delete(member);
    }

    @Transactional
    public void logout() {

    }

    public MemberRes getProfile(Member member) {
        return MemberRes.builder()
                .id(member.getMemberId())
                .email(member.getEmail())
                .name(member.getName())
                .build();
    }

    @Transactional
    public MemberRes updateProfile(Member member, MemberReq memberReq) {
        member.update(memberReq.name(), memberReq.skin_type());
        Member updateMember = memberRepository.save(member);

        return MemberRes.builder()
                .id(updateMember.getMemberId())
                .email(updateMember.getEmail())
                .name(updateMember.getName())
                .build();
    }

    // TODO: DB 회원가입 실패 시, 카카오에서도 회원 가입 실패로 보상 트랜잭션 처리 필요
    @Transactional // 독립적인 트랜잭션으로 실행, 상위 트랜잭션은 읽기 트랜잭션으로 유지
    public MemberRes register(String email, String nickname) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (nickname == null || nickname.isEmpty()) {
            throw new IllegalArgumentException("Nickname cannot be null or empty");
        }

        return memberRepository.findByEmail(email)
                .map(member ->
                        MemberRes.builder()
                                .id(member.getMemberId())
                                .email(member.getEmail())
                                .name(member.getName())
                                .build())
                .orElseGet(() -> {
                    Member newMember = new Member(nickname, email);
                    Member saveMember = memberRepository.save(newMember);
                    return MemberRes.builder()
                            .id(saveMember.getMemberId())
                            .email(saveMember.getEmail())
                            .name(saveMember.getName())
                            .build();
                });
    }
}