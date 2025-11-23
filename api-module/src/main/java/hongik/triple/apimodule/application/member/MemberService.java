package hongik.triple.apimodule.application.member;

import hongik.triple.apimodule.global.security.jwt.TokenProvider;
import hongik.triple.commonmodule.dto.member.MemberReq;
import hongik.triple.commonmodule.dto.member.MemberRes;
import hongik.triple.commonmodule.enumerate.MemberType;
import hongik.triple.domainmodule.domain.member.Member;
import hongik.triple.domainmodule.domain.member.repository.MemberRepository;
import hongik.triple.domainmodule.domain.survey.Survey;
import hongik.triple.domainmodule.domain.survey.repository.SurveyRepository;
import hongik.triple.inframodule.oauth.google.GoogleClient;
import hongik.triple.inframodule.oauth.google.GoogleProfile;
import hongik.triple.inframodule.oauth.google.GoogleToken;
import hongik.triple.inframodule.oauth.kakao.KakaoClient;
import hongik.triple.inframodule.oauth.kakao.KakaoProfile;
import hongik.triple.inframodule.oauth.kakao.KakaoToken;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final SurveyRepository surveyRepository;
    private final KakaoClient kakaoClient;
    private final GoogleClient googleClient;
    private final TokenProvider tokenProvider;

    public String getKakaoLoginUrl(String redirectUri) {
        return kakaoClient.getKakaoAuthUrl(redirectUri);
    }

    public String getGoogleLoginUrl(String redirectUri) {
        return googleClient.getGoogleAuthUrl(redirectUri);
    }

    public KakaoProfile loginWithKakao(String authorizationCode, String redirectUri) {
        KakaoToken kakaoToken = kakaoClient.getKakaoAccessToken(authorizationCode, redirectUri);
        return kakaoClient.getMemberInfo(kakaoToken);
    }

    public GoogleProfile loginWithGoogle(String authorizationCode, String redirectUri) {
        GoogleToken googleToken = googleClient.getGoogleAccessToken(authorizationCode, redirectUri);
        return googleClient.getMemberInfo(googleToken);
    }

    @Transactional
    public void withdrawal(Member member) {
        memberRepository.delete(member);
    }

    @Transactional
    public void logout() {

    }

    public MemberRes getProfile(Member member) {
        if (member.getSkinType() != null) {
            return MemberRes.builder()
                    .id(member.getMemberId())
                    .email(member.getEmail())
                    .name(member.getName())
                    .skinType(member.getSkinType())
                    .surveyTime(getLatestSurvey(member))
                    .build();
        }

        return MemberRes.builder()
                .id(member.getMemberId())
                .email(member.getEmail())
                .name(member.getName())
                .build();
    }

    @Transactional
    public MemberRes updateProfile(Member member, MemberReq memberReq) {
        member.updateSkinType(memberReq.skin_type());
        Member updateMember = memberRepository.save(member);

        return MemberRes.builder()
                .id(updateMember.getMemberId())
                .email(updateMember.getEmail())
                .name(updateMember.getName())
                .build();
    }

    // TODO: DB 회원가입 실패 시, 카카오에서도 회원 가입 실패로 보상 트랜잭션 처리 필요
    @Transactional // 독립적인 트랜잭션으로 실행, 상위 트랜잭션은 읽기 트랜잭션으로 유지
    public MemberRes register(String email, String nickname, MemberType memberType) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (nickname == null || nickname.isEmpty()) {
            throw new IllegalArgumentException("Nickname cannot be null or empty");
        }

        Member member = memberRepository.findByEmail(email)
                .orElseGet(() -> {
                    Member newMember = new Member(nickname, email, memberType);
                    return memberRepository.save(newMember);
                });

        String accessToken = tokenProvider.createToken(member).accessToken();

        return MemberRes.builder()
                .id(member.getMemberId())
                .email(member.getEmail())
                .name(member.getName())
                .accessToken(accessToken)
                .build();
    }

    private String getLatestSurvey(Member member) {
        Page<Survey> page = surveyRepository
                .findByMember_MemberIdOrderByCreatedAtDesc(member.getMemberId(), PageRequest.of(0, 1));

        return page.hasContent() ? formattedTime(page.getContent().get(0).getCreatedAt()) : null;
    }

    private String formattedTime(LocalDateTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return time.format(formatter);
    }
}