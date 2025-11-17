package hongik.triple.apimodule.survey;

import hongik.triple.apimodule.application.survey.SurveyService;
import hongik.triple.commonmodule.dto.survey.SurveyReq;
import hongik.triple.commonmodule.dto.survey.SurveyRes;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("SurveyService 단위 테스트")
@ExtendWith(MockitoExtension.class)
public class SurveyServiceTest {

    @InjectMocks
    private SurveyService surveyService;

    @Mock
    private SurveyRepository surveyRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private Member member;

    @Mock
    private Survey survey;

    @Captor
    private ArgumentCaptor<Survey> surveyCaptor;

    private Map<String, Object> validAnswers;
    private SurveyReq validSurveyReq;

    @BeforeEach
    void setUp() {
        // Member 설정
        when(member.getMemberId()).thenReturn(1L);
        when(member.getName()).thenReturn("홍길동");

        // 유효한 설문 답변 데이터 (Q001 ~ Q012)
        validAnswers = new HashMap<>();
        validAnswers.put("Q001", 3);
        validAnswers.put("Q002", 3);
        validAnswers.put("Q003", 4);
        validAnswers.put("Q004", 4);
        validAnswers.put("Q005", 3);
        validAnswers.put("Q006", 3);
        validAnswers.put("Q007", 4);
        validAnswers.put("Q008", 4);
        validAnswers.put("Q009", 4);
        validAnswers.put("Q010", 4);
        validAnswers.put("Q011", 2);
        validAnswers.put("Q012", 2);

        validSurveyReq = new SurveyReq(1L, validAnswers);

        // Survey Mock 설정
        when(survey.getSurveyId()).thenReturn(1L);
        when(survey.getMember()).thenReturn(member);
        when(survey.getSkinType()).thenReturn(String.valueOf(SkinType.OILY));
        when(survey.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(survey.getModifiedAt()).thenReturn(LocalDateTime.now());
    }

    @Nested
    @DisplayName("registerSurvey 테스트")
    class RegisterSurveyTest {

        @Test
        @DisplayName("성공: 정상적인 설문 등록 - OILY 타입")
        void registerSurvey_Success_OilyType() {
            // Given
            Map<String, Object> oilyAnswers = new HashMap<>();
            for (int i = 1; i <= 12; i++) {
                oilyAnswers.put(String.format("Q%03d", i), 1); // 모두 1점으로 설정
            }
            SurveyReq request = new SurveyReq(1L, oilyAnswers);

            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

            Survey savedSurvey = mock(Survey.class);
            when(savedSurvey.getSurveyId()).thenReturn(1L);
            when(savedSurvey.getMember()).thenReturn(member);
            when(savedSurvey.getSkinType()).thenReturn(String.valueOf(SkinType.OILY));
            when(savedSurvey.getCreatedAt()).thenReturn(LocalDateTime.now());
            when(savedSurvey.getModifiedAt()).thenReturn(LocalDateTime.now());

            Map<String, Object> processedBody = new HashMap<>();
            for (int i = 1; i <= 12; i++) {
                String qId = String.format("Q%03d", i);
                Map<String, Object> questionResult = new HashMap<>();
                questionResult.put("answer", 1);
                questionResult.put("score", 1);
                processedBody.put(qId, questionResult);
            }
            Map<String, Object> categoryScores = new HashMap<>();
            categoryScores.put("comedoneScore", 4);
            categoryScores.put("inflammationScore", 4);
            categoryScores.put("pustuleScore", 2);
            categoryScores.put("folliculitisScore", 2);
            processedBody.put("categoryScores", categoryScores);

            when(savedSurvey.getBody()).thenReturn(processedBody);
            when(surveyRepository.save(any())).thenReturn(savedSurvey);

            // When
            SurveyRes result = surveyService.registerSurvey(request);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.surveyId());
            assertEquals(1L, result.memberId());
            assertEquals("홍길동", result.memberName());
            assertEquals(SkinType.OILY, result.skinType());
            assertEquals(12, result.totalScore());
            assertNotNull(result.recommendation());

            verify(memberRepository, times(1)).findById(1L);
            verify(surveyRepository, times(1)).save(any());
        }

        @Test
        @DisplayName("성공: OILY 타입 - 높은 comedone 점수")
        void registerSurvey_Success_OilyType_HighComedoneScore() {
            // Given
            Map<String, Object> answers = new HashMap<>();
            answers.put("Q001", 4); // comedone
            answers.put("Q002", 4); // comedone
            answers.put("Q003", 2); // inflammation
            answers.put("Q004", 2); // inflammation
            answers.put("Q005", 4); // comedone
            answers.put("Q006", 4); // comedone
            answers.put("Q007", 2); // inflammation
            answers.put("Q008", 2); // inflammation
            answers.put("Q009", 2); // pustule
            answers.put("Q010", 2); // pustule
            answers.put("Q011", 2); // folliculitis
            answers.put("Q012", 2); // folliculitis

            SurveyReq request = new SurveyReq(1L, answers);

            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

            Survey savedSurvey = mock(Survey.class);
            when(savedSurvey.getSurveyId()).thenReturn(1L);
            when(savedSurvey.getMember()).thenReturn(member);
            when(savedSurvey.getSkinType()).thenReturn(String.valueOf(SkinType.OILY));
            when(savedSurvey.getCreatedAt()).thenReturn(LocalDateTime.now());
            when(savedSurvey.getModifiedAt()).thenReturn(LocalDateTime.now());

            Map<String, Object> processedBody = new HashMap<>();
            when(savedSurvey.getBody()).thenReturn(processedBody);
            when(surveyRepository.save(any())).thenReturn(savedSurvey);

            // When
            SurveyRes result = surveyService.registerSurvey(request);

            // Then
            assertNotNull(result);
            assertEquals(SkinType.OILY, result.skinType());
        }

        @Test
        @DisplayName("성공: OILY 타입 - 높은 inflammation 점수")
        void registerSurvey_Success_OilyType_HighInflammationScore() {
            // Given
            Map<String, Object> answers = new HashMap<>();
            answers.put("Q001", 2); // comedone
            answers.put("Q002", 2); // comedone
            answers.put("Q003", 5); // inflammation
            answers.put("Q004", 5); // inflammation
            answers.put("Q005", 2); // comedone
            answers.put("Q006", 2); // comedone
            answers.put("Q007", 5); // inflammation
            answers.put("Q008", 5); // inflammation
            answers.put("Q009", 2); // pustule
            answers.put("Q010", 2); // pustule
            answers.put("Q011", 2); // folliculitis
            answers.put("Q012", 2); // folliculitis

            SurveyReq request = new SurveyReq(1L, answers);

            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

            Survey savedSurvey = mock(Survey.class);
            when(savedSurvey.getSurveyId()).thenReturn(1L);
            when(savedSurvey.getMember()).thenReturn(member);
            when(savedSurvey.getSkinType()).thenReturn(String.valueOf(SkinType.OILY));
            when(savedSurvey.getCreatedAt()).thenReturn(LocalDateTime.now());
            when(savedSurvey.getModifiedAt()).thenReturn(LocalDateTime.now());

            Map<String, Object> processedBody = new HashMap<>();
            when(savedSurvey.getBody()).thenReturn(processedBody);
            when(surveyRepository.save(any())).thenReturn(savedSurvey);

            // When
            SurveyRes result = surveyService.registerSurvey(request);

            // Then
            assertNotNull(result);
            assertEquals(SkinType.OILY, result.skinType());
        }

        @Test
        @DisplayName("성공: OILY 타입 - 높은 pustule 점수")
        void registerSurvey_Success_OilyType_HighPustuleScore() {
            // Given
            Map<String, Object> answers = new HashMap<>();
            answers.put("Q001", 2);
            answers.put("Q002", 2);
            answers.put("Q003", 2);
            answers.put("Q004", 2);
            answers.put("Q005", 2);
            answers.put("Q006", 2);
            answers.put("Q007", 2);
            answers.put("Q008", 2);
            answers.put("Q009", 5); // pustule
            answers.put("Q010", 5); // pustule
            answers.put("Q011", 2);
            answers.put("Q012", 2);

            SurveyReq request = new SurveyReq(1L, answers);

            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

            Survey savedSurvey = mock(Survey.class);
            when(savedSurvey.getSurveyId()).thenReturn(1L);
            when(savedSurvey.getMember()).thenReturn(member);
            when(savedSurvey.getSkinType()).thenReturn(SkinType.OILY);
            when(savedSurvey.getCreatedAt()).thenReturn(LocalDateTime.now());
            when(savedSurvey.getModifiedAt()).thenReturn(LocalDateTime.now());

            Map<String, Object> processedBody = new HashMap<>();
            when(savedSurvey.getBody()).thenReturn(processedBody);
            when(surveyRepository.save(any())).thenReturn(savedSurvey);

            // When
            SurveyRes result = surveyService.registerSurvey(request);

            // Then
            assertNotNull(result);
            assertEquals(SkinType.OILY, result.skinType());
        }

        @Test
        @DisplayName("성공: OILY 타입 - 높은 folliculitis 점수")
        void registerSurvey_Success_OilyType_HighFolliculitisScore() {
            // Given
            Map<String, Object> answers = new HashMap<>();
            answers.put("Q001", 2);
            answers.put("Q002", 2);
            answers.put("Q003", 2);
            answers.put("Q004", 2);
            answers.put("Q005", 2);
            answers.put("Q006", 2);
            answers.put("Q007", 2);
            answers.put("Q008", 2);
            answers.put("Q009", 2);
            answers.put("Q010", 2);
            answers.put("Q011", 5); // folliculitis
            answers.put("Q012", 5); // folliculitis

            SurveyReq request = new SurveyReq(1L, answers);

            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

            Survey savedSurvey = mock(Survey.class);
            when(savedSurvey.getSurveyId()).thenReturn(1L);
            when(savedSurvey.getMember()).thenReturn(member);
            when(savedSurvey.getSkinType()).thenReturn(SkinType.OILY);
            when(savedSurvey.getCreatedAt()).thenReturn(LocalDateTime.now());
            when(savedSurvey.getModifiedAt()).thenReturn(LocalDateTime.now());

            Map<String, Object> processedBody = new HashMap<>();
            when(savedSurvey.getBody()).thenReturn(processedBody);
            when(surveyRepository.save(any())).thenReturn(savedSurvey);

            // When
            SurveyRes result = surveyService.registerSurvey(request);

            // Then
            assertNotNull(result);
            assertEquals(SkinType.OILY, result.skinType());
        }

        @Test
        @DisplayName("실패: 존재하지 않는 회원")
        void registerSurvey_Fail_MemberNotFound() {
            // Given
            when(memberRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> surveyService.registerSurvey(validSurveyReq)
            );

            assertEquals("존재하지 않는 회원입니다.", exception.getMessage());
            verify(memberRepository, times(1)).findById(1L);
            verify(surveyRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패: 필수 질문 누락")
        void registerSurvey_Fail_MissingRequiredQuestion() {
            // Given
            Map<String, Object> incompleteAnswers = new HashMap<>();
            incompleteAnswers.put("Q001", 3);
            incompleteAnswers.put("Q002", 3);
            // Q003 ~ Q012 누락

            SurveyReq request = new SurveyReq(1L, incompleteAnswers);
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> surveyService.registerSurvey(request)
            );

            assertTrue(exception.getMessage().contains("필수 질문에 대한 답변이 없습니다"));
            verify(surveyRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패: 점수 범위 초과 (6점)")
        void registerSurvey_Fail_ScoreOutOfRange_Over() {
            // Given
            Map<String, Object> invalidAnswers = new HashMap<>(validAnswers);
            invalidAnswers.put("Q001", 6); // 유효 범위: 1-5

            SurveyReq request = new SurveyReq(1L, invalidAnswers);
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> surveyService.registerSurvey(request)
            );

            assertTrue(exception.getMessage().contains("점수는 1-5 범위여야 합니다"));
            verify(surveyRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패: 점수 범위 미달 (0점)")
        void registerSurvey_Fail_ScoreOutOfRange_Under() {
            // Given
            Map<String, Object> invalidAnswers = new HashMap<>(validAnswers);
            invalidAnswers.put("Q001", 0); // 유효 범위: 1-5

            SurveyReq request = new SurveyReq(1L, invalidAnswers);
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> surveyService.registerSurvey(request)
            );

            assertTrue(exception.getMessage().contains("점수는 1-5 범위여야 합니다"));
            verify(surveyRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패: 유효하지 않은 점수 형식 (문자열)")
        void registerSurvey_Fail_InvalidScoreFormat() {
            // Given
            Map<String, Object> invalidAnswers = new HashMap<>(validAnswers);
            invalidAnswers.put("Q001", "invalid");

            SurveyReq request = new SurveyReq(1L, invalidAnswers);
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> surveyService.registerSurvey(request)
            );

            assertTrue(exception.getMessage().contains("유효하지 않은 점수 형식입니다"));
            verify(surveyRepository, never()).save(any());
        }

        @Test
        @DisplayName("성공: ArgumentCaptor를 사용한 저장 데이터 검증")
        void registerSurvey_Success_VerifyWithCaptor() {
            // Given
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

            Survey savedSurvey = mock(Survey.class);
            when(savedSurvey.getSurveyId()).thenReturn(1L);
            when(savedSurvey.getMember()).thenReturn(member);
            when(savedSurvey.getSkinType()).thenReturn(SkinType.OILY);
            when(savedSurvey.getCreatedAt()).thenReturn(LocalDateTime.now());
            when(savedSurvey.getModifiedAt()).thenReturn(LocalDateTime.now());

            Map<String, Object> processedBody = new HashMap<>();
            when(savedSurvey.getBody()).thenReturn(processedBody);
            when(surveyRepository.save(any())).thenReturn(savedSurvey);

            // When
            SurveyRes result = surveyService.registerSurvey(validSurveyReq);

            // Then
            verify(surveyRepository, times(1)).save(surveyCaptor.capture());
            Survey capturedSurvey = surveyCaptor.getValue();
            assertNotNull(capturedSurvey);
        }
    }

    @Nested
    @DisplayName("getSurveyQuestions 테스트")
    class GetSurveyQuestionsTest {

        @Test
        @DisplayName("성공: 설문 질문 목록 조회")
        void getSurveyQuestions_Success() {
            // When
            SurveyRes result = surveyService.getSurveyQuestions();

            // Then
            assertNotNull(result);
            assertNotNull(result.questions());
            assertEquals(12, result.questions().size());

            // 첫 번째 질문 검증
            SurveyQuestionDto firstQuestion = result.questions().get(0);
            assertEquals("Q001", firstQuestion.questionId());
            assertTrue(firstQuestion.required());
            assertEquals("SCALE", firstQuestion.questionType());
            assertEquals(5, firstQuestion.options().size());
        }

        @Test
        @DisplayName("성공: 모든 질문이 필수 항목")
        void getSurveyQuestions_Success_AllRequired() {
            // When
            SurveyRes result = surveyService.getSurveyQuestions();

            // Then
            result.questions().forEach(question -> {
                assertTrue(question.required(),
                        "Question " + question.questionId() + " should be required");
            });
        }

        @Test
        @DisplayName("성공: 질문 순서 검증")
        void getSurveyQuestions_Success_VerifyOrder() {
            // When
            SurveyRes result = surveyService.getSurveyQuestions();

            // Then
            List<SurveyQuestionDto> questions = result.questions();
            for (int i = 0; i < questions.size(); i++) {
                assertEquals(i + 1, questions.get(i).orderNumber());
            }
        }
    }

    @Nested
    @DisplayName("getSurveyList 테스트")
    class GetSurveyListTest {

        private Pageable pageable;

        @BeforeEach
        void setUp() {
            pageable = PageRequest.of(0, 10);
        }

        @Test
        @DisplayName("성공: 특정 회원의 설문 목록 조회")
        void getSurveyList_Success_ByMember() {
            // Given
            Survey survey1 = createMockSurvey(1L, SkinType.OILY);
            Survey survey2 = createMockSurvey(2L, SkinType.OILY);

            List<Survey> surveys = Arrays.asList(survey1, survey2);
            Page<Survey> surveyPage = new PageImpl<>(surveys, pageable, surveys.size());

            when(surveyRepository.findByMember_MemberIdOrderByCreatedAtDesc(1L, pageable))
                    .thenReturn(surveyPage);

            // When
            Page<SurveyRes> result = surveyService.getSurveyList(member, pageable);

            // Then
            assertNotNull(result);
            assertEquals(2, result.getContent().size());
            verify(surveyRepository, times(1))
                    .findByMember_MemberIdOrderByCreatedAtDesc(1L, pageable);
        }

        @Test
        @DisplayName("성공: 전체 설문 목록 조회 (memberId가 null)")
        void getSurveyList_Success_AllSurveys() {
            // Given
            Member memberWithNullId = mock(Member.class);
            when(memberWithNullId.getMemberId()).thenReturn(null);

            Survey survey1 = createMockSurvey(1L, SkinType.OILY);
            Survey survey2 = createMockSurvey(2L, SkinType.OILY);

            List<Survey> surveys = Arrays.asList(survey1, survey2);
            Page<Survey> surveyPage = new PageImpl<>(surveys, pageable, surveys.size());

            when(surveyRepository.findAllByOrderByCreatedAtDesc(pageable))
                    .thenReturn(surveyPage);

            // When
            Page<SurveyRes> result = surveyService.getSurveyList(memberWithNullId, pageable);

            // Then
            assertNotNull(result);
            assertEquals(2, result.getContent().size());
            verify(surveyRepository, times(1))
                    .findAllByOrderByCreatedAtDesc(pageable);
        }

        @Test
        @DisplayName("성공: 빈 페이지 반환")
        void getSurveyList_Success_EmptyPage() {
            // Given
            Page<Survey> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            when(surveyRepository.findByMember_MemberIdOrderByCreatedAtDesc(1L, pageable))
                    .thenReturn(emptyPage);

            // When
            Page<SurveyRes> result = surveyService.getSurveyList(member, pageable);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            assertEquals(0, result.getContent().size());
        }
    }

    @Nested
    @DisplayName("getSurveyDetail 테스트")
    class GetSurveyDetailTest {

        @Test
        @DisplayName("성공: 설문 상세 조회")
        void getSurveyDetail_Success() {
            // Given
            Long surveyId = 1L;
            Survey mockSurvey = createMockSurvey(surveyId, SkinType.OILY);

            when(surveyRepository.findById(surveyId)).thenReturn(Optional.of(mockSurvey));

            // When
            SurveyRes result = surveyService.getSurveyDetail(surveyId);

            // Then
            assertNotNull(result);
            assertEquals(surveyId, result.surveyId());
            assertEquals(1L, result.memberId());
            assertEquals("홍길동", result.memberName());
            assertEquals(SkinType.OILY, result.skinType());
            assertNotNull(result.recommendation());

            verify(surveyRepository, times(1)).findById(surveyId);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 설문")
        void getSurveyDetail_Fail_SurveyNotFound() {
            // Given
            Long surveyId = 999L;
            when(surveyRepository.findById(surveyId)).thenReturn(Optional.empty());

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> surveyService.getSurveyDetail(surveyId)
            );

            assertEquals("존재하지 않는 설문조사입니다.", exception.getMessage());
            verify(surveyRepository, times(1)).findById(surveyId);
        }
    }

    @Nested
    @DisplayName("피부 타입 계산 로직 테스트")
    class SkinTypeCalculationTest {

        @Test
        @DisplayName("성공: 매우 낮은 점수 - OILY")
        void calculateSkinType_VeryLowScore_Oily() {
            // Given
            Map<String, Object> lowScoreAnswers = new HashMap<>();
            for (int i = 1; i <= 12; i++) {
                lowScoreAnswers.put(String.format("Q%03d", i), 1);
            }

            SurveyReq request = new SurveyReq(1L, lowScoreAnswers);
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

            Survey savedSurvey = mock(Survey.class);
            when(savedSurvey.getSurveyId()).thenReturn(1L);
            when(savedSurvey.getMember()).thenReturn(member);
            when(savedSurvey.getSkinType()).thenReturn(SkinType.OILY);
            when(savedSurvey.getCreatedAt()).thenReturn(LocalDateTime.now());
            when(savedSurvey.getModifiedAt()).thenReturn(LocalDateTime.now());

            Map<String, Object> processedBody = new HashMap<>();
            when(savedSurvey.getBody()).thenReturn(processedBody);
            when(surveyRepository.save(any())).thenReturn(savedSurvey);

            // When
            SurveyRes result = surveyService.registerSurvey(request);

            // Then
            assertEquals(SkinType.OILY, result.skinType());
        }

        @Test
        @DisplayName("성공: 중간 점수 - OILY (기본값)")
        void calculateSkinType_MediumScore_Oily() {
            // Given - validAnswers 사용 (모두 3-4점)
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

            Survey savedSurvey = mock(Survey.class);
            when(savedSurvey.getSurveyId()).thenReturn(1L);
            when(savedSurvey.getMember()).thenReturn(member);
            when(savedSurvey.getSkinType()).thenReturn(SkinType.OILY);
            when(savedSurvey.getCreatedAt()).thenReturn(LocalDateTime.now());
            when(savedSurvey.getModifiedAt()).thenReturn(LocalDateTime.now());

            Map<String, Object> processedBody = new HashMap<>();
            when(savedSurvey.getBody()).thenReturn(processedBody);
            when(surveyRepository.save(any())).thenReturn(savedSurvey);

            // When
            SurveyRes result = surveyService.registerSurvey(validSurveyReq);

            // Then
            assertEquals(SkinType.OILY, result.skinType());
        }
    }

    @Nested
    @DisplayName("추천 메시지 생성 테스트")
    class RecommendationTest {

        @Test
        @DisplayName("성공: OILY 타입 추천 메시지")
        void generateRecommendation_OilyType() {
            // Given
            Survey mockSurvey = createMockSurvey(1L, SkinType.OILY);
            when(surveyRepository.findById(1L)).thenReturn(Optional.of(mockSurvey));

            // When
            SurveyRes result = surveyService.getSurveyDetail(1L);

            // Then
            assertNotNull(result.recommendation());
            assertTrue(result.recommendation().contains("양호"));
        }

        @Test
        @DisplayName("성공: COMBINATION 타입 추천 메시지")
        void generateRecommendation_CombinationType() {
            // Given
            Survey mockSurvey = createMockSurvey(1L, SkinType.COMBINATION);
            when(surveyRepository.findById(1L)).thenReturn(Optional.of(mockSurvey));

            // When
            SurveyRes result = surveyService.getSurveyDetail(1L);

            // Then
            assertNotNull(result.recommendation());
            assertTrue(result.recommendation().contains("좁쌀여드름"));
        }

        @Test
        @DisplayName("성공: DRY 타입 추천 메시지")
        void generateRecommendation_DryType() {
            // Given
            Survey mockSurvey = createMockSurvey(1L, SkinType.DRY);
            when(surveyRepository.findById(1L)).thenReturn(Optional.of(mockSurvey));

            // When
            SurveyRes result = surveyService.getSurveyDetail(1L);

            // Then
            assertNotNull(result.recommendation());
            assertTrue(result.recommendation().contains("화농성"));
        }
    }

    @Nested
    @DisplayName("총점 계산 테스트")
    class TotalScoreCalculationTest {

        @Test
        @DisplayName("성공: 총점 계산")
        void calculateTotalScore_Success() {
            // Given
            Map<String, Object> body = new HashMap<>();

            // Q001-Q012 각 3점씩 = 36점
            for (int i = 1; i <= 12; i++) {
                String qId = String.format("Q%03d", i);
                Map<String, Object> questionResult = new HashMap<>();
                questionResult.put("answer", 3);
                questionResult.put("score", 3);
                body.put(qId, questionResult);
            }

            Map<String, Object> categoryScores = new HashMap<>();
            categoryScores.put("comedoneScore", 12);
            categoryScores.put("inflammationScore", 12);
            categoryScores.put("pustuleScore", 6);
            categoryScores.put("folliculitisScore", 6);
            body.put("categoryScores", categoryScores);

            Survey mockSurvey = mock(Survey.class);
            when(mockSurvey.getSurveyId()).thenReturn(1L);
            when(mockSurvey.getMember()).thenReturn(member);
            when(mockSurvey.getSkinType()).thenReturn(SkinType.OILY);
            when(mockSurvey.getBody()).thenReturn(body);
            when(mockSurvey.getCreatedAt()).thenReturn(LocalDateTime.now());
            when(mockSurvey.getModifiedAt()).thenReturn(LocalDateTime.now());

            when(surveyRepository.findById(1L)).thenReturn(Optional.of(mockSurvey));

            // When
            SurveyRes result = surveyService.getSurveyDetail(1L);

            // Then
            assertEquals(36, result.totalScore());
        }

        @Test
        @DisplayName("성공: 최소 점수 (12점)")
        void calculateTotalScore_MinimumScore() {
            // Given
            Map<String, Object> body = new HashMap<>();

            // Q001-Q012 각 1점씩 = 12점
            for (int i = 1; i <= 12; i++) {
                String qId = String.format("Q%03d", i);
                Map<String, Object> questionResult = new HashMap<>();
                questionResult.put("answer", 1);
                questionResult.put("score", 1);
                body.put(qId, questionResult);
            }

            Survey mockSurvey = mock(Survey.class);
            when(mockSurvey.getSurveyId()).thenReturn(1L);
            when(mockSurvey.getMember()).thenReturn(member);
            when(mockSurvey.getSkinType()).thenReturn(SkinType.OILY);
            when(mockSurvey.getBody()).thenReturn(body);
            when(mockSurvey.getCreatedAt()).thenReturn(LocalDateTime.now());
            when(mockSurvey.getModifiedAt()).thenReturn(LocalDateTime.now());

            when(surveyRepository.findById(1L)).thenReturn(Optional.of(mockSurvey));

            // When
            SurveyRes result = surveyService.getSurveyDetail(1L);

            // Then
            assertEquals(12, result.totalScore());
        }

        @Test
        @DisplayName("성공: 최대 점수 (60점)")
        void calculateTotalScore_MaximumScore() {
            // Given
            Map<String, Object> body = new HashMap<>();

            // Q001-Q012 각 5점씩 = 60점
            for (int i = 1; i <= 12; i++) {
                String qId = String.format("Q%03d", i);
                Map<String, Object> questionResult = new HashMap<>();
                questionResult.put("answer", 5);
                questionResult.put("score", 5);
                body.put(qId, questionResult);
            }

            Survey mockSurvey = mock(Survey.class);
            when(mockSurvey.getSurveyId()).thenReturn(1L);
            when(mockSurvey.getMember()).thenReturn(member);
            when(mockSurvey.getSkinType()).thenReturn(SkinType.OILY);
            when(mockSurvey.getBody()).thenReturn(body);
            when(mockSurvey.getCreatedAt()).thenReturn(LocalDateTime.now());
            when(mockSurvey.getModifiedAt()).thenReturn(LocalDateTime.now());

            when(surveyRepository.findById(1L)).thenReturn(Optional.of(mockSurvey));

            // When
            SurveyRes result = surveyService.getSurveyDetail(1L);

            // Then
            assertEquals(60, result.totalScore());
        }
    }

    // Helper methods
    private Survey createMockSurvey(Long surveyId, SkinType skinType) {
        Survey mockSurvey = mock(Survey.class);

        Map<String, Object> body = new HashMap<>();
        for (int i = 1; i <= 12; i++) {
            String qId = String.format("Q%03d", i);
            Map<String, Object> questionResult = new HashMap<>();
            questionResult.put("answer", 3);
            questionResult.put("score", 3);
            body.put(qId, questionResult);
        }

        Map<String, Object> categoryScores = new HashMap<>();
        categoryScores.put("comedoneScore", 12);
        categoryScores.put("inflammationScore", 12);
        categoryScores.put("pustuleScore", 6);
        categoryScores.put("folliculitisScore", 6);
        body.put("categoryScores", categoryScores);

        when(mockSurvey.getSurveyId()).thenReturn(surveyId);
        when(mockSurvey.getMember()).thenReturn(member);
        when(mockSurvey.getSkinType()).thenReturn(skinType);
        when(mockSurvey.getBody()).thenReturn(body);
        when(mockSurvey.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(mockSurvey.getModifiedAt()).thenReturn(LocalDateTime.now());

        return mockSurvey;
    }
}