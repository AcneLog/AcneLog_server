package hongik.triple.apimodule.survey;

import hongik.triple.apimodule.application.survey.SurveyService;
import hongik.triple.commonmodule.dto.survey.SurveyReq;
import hongik.triple.commonmodule.dto.survey.SurveyRes;
import hongik.triple.commonmodule.enumerate.MemberType;
import hongik.triple.commonmodule.enumerate.SkinType;
import hongik.triple.domainmodule.domain.member.Member;
import hongik.triple.domainmodule.domain.member.repository.MemberRepository;
import hongik.triple.domainmodule.domain.survey.Survey;
import hongik.triple.domainmodule.domain.survey.repository.SurveyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@DisplayName("SurveyService 테스트")
@ExtendWith(MockitoExtension.class)
class SurveyServiceTest {

    @Mock
    private SurveyRepository surveyRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private SurveyService surveyService;

    private Member member;

    @BeforeEach
    void setup() {
        member = new Member("user", "email@test.com", MemberType.KAKAO);
        ReflectionTestUtils.setField(member, "memberId", 1L);
    }

    private Map<String, Object> buildValidAnswers() {
        Map<String, Object> answers = new HashMap<>();
        for (int i = 1; i <= 12; i++) {
            answers.put(String.format("Q%03d", i), 3); // 모든 값을 3으로 설정
        }
        return answers;
    }

    @Nested
    @DisplayName("registerSurvey()는")
    class RegisterSurveyTest {

        @Test
        @DisplayName("유효한 설문 응답을 저장하고 SurveyRes를 반환한다.")
        void success() {
            // given
            Map<String, Object> answers = buildValidAnswers();
            SurveyReq req = new SurveyReq(answers);

            Survey survey = Survey.builder()
                    .member(member)
                    .body(answers)
                    .skinType(SkinType.COMBINATION)
                    .build();
            ReflectionTestUtils.setField(survey, "surveyId", 10L);

            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(surveyRepository.save(any(Survey.class))).willReturn(survey);

            // when
            SurveyRes result = surveyService.registerSurvey(member, req);

            // then
            assertThat(result.surveyId()).isEqualTo(10L);
            assertThat(result.memberId()).isEqualTo(1L);
            assertThat(result.skinType()).isEqualTo(SkinType.COMBINATION.getDescription());
            assertThat(result.questions()).hasSize(12);
            assertThat(result.totalScore()).isGreaterThan(0);

            verify(surveyRepository, times(1)).save(any());
        }

        @Test
        @DisplayName("사용자를 찾을 수 없으면 예외를 던진다.")
        void memberNotFound() {
            // given
            SurveyReq req = new SurveyReq(buildValidAnswers());
            given(memberRepository.findById(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> surveyService.registerSurvey(member, req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("존재하지 않는 사용자");

            verify(surveyRepository, never()).save(any());
        }

        @Test
        @DisplayName("필수 답변이 빠져 있으면 예외를 던진다.")
        void missingRequiredQuestions() {
            // given
            Map<String, Object> answers = buildValidAnswers();
            answers.remove("Q001"); // 필수 항목 제거

            SurveyReq req = new SurveyReq(answers);

            given(memberRepository.findById(1L)).willReturn(Optional.of(member));

            // when & then
            assertThatThrownBy(() -> surveyService.registerSurvey(member, req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("필수 질문");

            verify(surveyRepository, never()).save(any());
        }

        @Test
        @DisplayName("점수가 1~5 범위를 벗어나면 예외를 던진다.")
        void invalidScore() {
            Map<String, Object> answers = buildValidAnswers();
            answers.put("Q005", 99); // invalid

            SurveyReq req = new SurveyReq(answers);

            given(memberRepository.findById(1L)).willReturn(Optional.of(member));

            assertThatThrownBy(() -> surveyService.registerSurvey(member, req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("점수는 1-5 범위");

            verify(surveyRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getSurveyQuestions()는")
    class GetSurveyQuestionsTest {

        @Test
        @DisplayName("설문 질문 목록을 반환한다.")
        void success() {
            // when
            SurveyRes result = surveyService.getSurveyQuestions();

            // then
            assertThat(result.questions()).isNotEmpty();
            assertThat(result.questions()).hasSize(12);
        }
    }

    @Nested
    @DisplayName("getSurveyList()는")
    class GetSurveyListTest {

        @Test
        @DisplayName("회원 ID가 있을 경우 해당 회원의 설문 목록을 반환한다.")
        void successMemberCase() {
            Pageable pageable = PageRequest.of(0, 10);
            Survey survey = Survey.builder()
                    .member(member)
                    .body(buildValidAnswers())
                    .skinType(SkinType.OILY)
                    .build();

            Page<Survey> page = new PageImpl<>(List.of(survey), pageable, 1);

            given(surveyRepository.findByMember_MemberIdOrderByCreatedAtDesc(1L, pageable))
                    .willReturn(page);

            Page<SurveyRes> result = surveyService.getSurveyList(member, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).skinType()).isEqualTo(SkinType.OILY.getDescription());
        }

        @Test
        @DisplayName("회원 ID가 null이면 전체 목록을 반환한다.")
        void successAllCase() {
            Member noIdMember = new Member("user", "email", MemberType.KAKAO);
            Pageable pageable = PageRequest.of(0, 10);

            Survey survey = Survey.builder()
                    .member(member)
                    .body(buildValidAnswers())
                    .skinType(SkinType.DRY)
                    .build();

            Page<Survey> page = new PageImpl<>(List.of(survey), pageable, 1);

            given(surveyRepository.findAllByOrderByCreatedAtDesc(pageable))
                    .willReturn(page);

            Page<SurveyRes> result = surveyService.getSurveyList(noIdMember, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).skinType()).isEqualTo(SkinType.DRY.getDescription());
        }
    }

    @Nested
    @DisplayName("getSurveyDetail()은")
    class GetSurveyDetailTest {

        @Test
        @DisplayName("설문 상세 정보를 반환한다.")
        void success() {
            Survey survey = Survey.builder()
                    .member(member)
                    .body(buildValidAnswers())
                    .skinType(SkinType.COMBINATION)
                    .build();
            ReflectionTestUtils.setField(survey, "surveyId", 99L);

            given(surveyRepository.findById(99L)).willReturn(Optional.of(survey));

            SurveyRes result = surveyService.getSurveyDetail(99L);

            assertThat(result.surveyId()).isEqualTo(99L);
            assertThat(result.skinType()).isEqualTo(SkinType.COMBINATION.getDescription());
        }

        @Test
        @DisplayName("설문이 존재하지 않으면 예외를 던진다.")
        void notFound() {
            given(surveyRepository.findById(10L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> surveyService.getSurveyDetail(10L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("존재하지 않는 설문조사");
        }
    }
}
