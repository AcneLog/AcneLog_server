package hongik.triple.apimodule.application.survey;

import hongik.triple.commonmodule.dto.survey.SurveyOptionDto;
import hongik.triple.commonmodule.dto.survey.SurveyQuestionDto;
import hongik.triple.commonmodule.dto.survey.SurveyReq;
import hongik.triple.commonmodule.dto.survey.SurveyRes;
import hongik.triple.commonmodule.enumerate.SkinType;
import hongik.triple.domainmodule.domain.member.Member;
import hongik.triple.domainmodule.domain.member.repository.MemberRepository;
import hongik.triple.domainmodule.domain.survey.Survey;
import hongik.triple.domainmodule.domain.survey.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public SurveyRes registerSurvey(SurveyReq request) {
        // Validation
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        validateSurveyAnswers(request.answers());

        // Business Logic
        SkinType skinType = calculateSkinType(request.answers());
        Map<String, Object> processedBody = processSurveyBody(request.answers());

        Survey survey = Survey.builder()
                .member(member)
                .body(processedBody)
                .skinType(skinType)
                .build();

        Survey savedSurvey = surveyRepository.save(survey);

        // Response
        return SurveyRes.builder()
                .surveyId(savedSurvey.getSurveyId())
                .memberId(savedSurvey.getMember().getMemberId())
                .memberName(savedSurvey.getMember().getName())
                .skinType(savedSurvey.getSkinType())
                .body((Map<String, Object>) savedSurvey.getBody())
                .createdAt(savedSurvey.getCreatedAt())
                .modifiedAt(savedSurvey.getModifiedAt())
                .totalScore(calculateTotalScore(processedBody))
                .recommendation(generateRecommendation(skinType))
                .build();
    }

    public SurveyRes getSurveyQuestions() {
        List<SurveyQuestionDto> questions = buildSurveyQuestions();

        return SurveyRes.builder()
                .questions(questions)
                .build();
    }

    public Page<SurveyRes> getSurveyList(Long memberId, Pageable pageable) {
        // Validation
        if (memberId != null) {
            memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        }

        // Business Logic
        Page<Survey> surveys = (memberId != null)
                ? surveyRepository.findByMember_MemberIdOrderByCreatedAtDesc(memberId, pageable)
                : surveyRepository.findAllByOrderByCreatedAtDesc(pageable);

        // Response
        return surveys.map(this::convertToSurveyRes);
    }

    public SurveyRes getSurveyDetail(Long surveyId) {
        // Validation
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 설문조사입니다."));

        // Business Logic & Response
        return convertToSurveyRes(survey);
    }

    // Private helper methods
    private void validateSurveyAnswers(Map<String, Object> answers) {
        List<SurveyQuestionDto> questions = buildSurveyQuestions();

        for (SurveyQuestionDto question : questions) {
            if (question.isRequired() && !answers.containsKey(question.getQuestionId())) {
                throw new IllegalArgumentException("필수 질문에 대한 답변이 없습니다: " + question.getQuestionText());
            }
        }
    }

    private SkinType calculateSkinType(Map<String, Object> answers) {
        int totalScore = 0;
        int answerCount = 0;

        // 각 질문별 점수 계산
        for (Map.Entry<String, Object> entry : answers.entrySet()) {
            String questionId = entry.getKey();
            Object answer = entry.getValue();

            int score = calculateQuestionScore(questionId, answer);
            if (score > 0) {
                totalScore += score;
                answerCount++;
            }
        }

        // 평균 점수 계산
        double averageScore = answerCount > 0 ? (double) totalScore / answerCount : 0;

        // 특정 문항들의 가중치를 고려한 세부 판단
        int inflammationScore = getInflammationRelatedScore(answers); // Q003, Q004, Q007, Q008
        int comedoneScore = getComedoneRelatedScore(answers); // Q001, Q002, Q005, Q006
        int pustuleScore = getPustuleRelatedScore(answers); // Q009, Q010
        int folliculitisScore = getFolliculitisRelatedScore(answers); // Q011, Q012

        // 점수 기반 피부 타입 결정
        if (averageScore <= 2.0) {
            return SkinType.NORMAL;
        } else if (inflammationScore >= 15) { // 염증성 문항 평균 4점 이상
            return SkinType.PAPULES;
        } else if (pustuleScore >= 8) { // 화농성 문항 평균 4점 이상
            return SkinType.PUSTULES;
        } else if (folliculitisScore >= 8) { // 모낭염 문항 평균 4점 이상
            return SkinType.FOLLICULITIS;
        } else if (comedoneScore >= 12) { // 좁쌀 문항 평균 3점 이상
            return SkinType.COMEDONES;
        } else {
            return SkinType.NORMAL;
        }
    }

    private int calculateQuestionScore(String questionId, Object answer) {
        // 질문별 점수 계산 로직
        switch (questionId) {
            case "Q001": // 세안 후 피부 상태
                return getScaleScore(answer, 5); // 1-5 척도
            case "Q002": // 하루 종일 피부 느낌
                return getMultipleChoiceScore(answer, Map.of(
                        "very_oily", 1,
                        "slightly_oily", 2,
                        "normal", 3,
                        "slightly_dry", 4,
                        "very_dry", 5
                ));
            case "Q003": // T존 상태
                return getScaleScore(answer, 5);
            case "Q004": // 모공 크기
                return getMultipleChoiceScore(answer, Map.of(
                        "very_large", 1,
                        "large", 2,
                        "medium", 3,
                        "small", 4,
                        "very_small", 5
                ));
            case "Q005": // 트러블 빈도
                return getScaleScore(answer, 5);
            default:
                return 0;
        }
    }

    private int getScaleScore(Object answer, int maxScale) {
        try {
            int score = Integer.parseInt(answer.toString());
            return Math.min(Math.max(score, 1), maxScale);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private int getMultipleChoiceScore(Object answer, Map<String, Integer> scoreMap) {
        return scoreMap.getOrDefault(answer.toString(), 1);
    }

    private Map<String, Object> processSurveyBody(Map<String, Object> answers) {
        Map<String, Object> processedBody = new HashMap<>();

        for (Map.Entry<String, Object> entry : answers.entrySet()) {
            String questionId = entry.getKey();
            Object answer = entry.getValue();
            int score = calculateQuestionScore(questionId, answer);

            Map<String, Object> questionResult = new HashMap<>();
            questionResult.put("answer", answer);
            questionResult.put("score", score);

            processedBody.put(questionId, questionResult);
        }

        return processedBody;
    }

    private int calculateTotalScore(Map<String, Object> body) {
        return body.values().stream()
                .mapToInt(value -> {
                    if (value instanceof Map) {
                        Map<String, Object> questionResult = (Map<String, Object>) value;
                        return (Integer) questionResult.getOrDefault("score", 0);
                    }
                    return 0;
                })
                .sum();
    }

    private String generateRecommendation(SkinType skinType) {
        switch (skinType) {
            case DRY:
                return "보습에 중점을 둔 스킨케어를 추천합니다.";
            case OILY:
                return "유분 조절과 모공 관리에 집중하세요.";
            case COMBINATION:
                return "부위별 맞춤 케어가 필요합니다.";
            case NORMAL:
                return "현재 상태를 유지하는 기본 케어를 권장합니다.";
            default:
                return "전문가와 상담을 받아보세요.";
        }
    }

    private List<SurveyQuestionDto> buildSurveyQuestions() {
        return Arrays.asList(
                new SurveyQuestionDto(
                        "Q001",
                        "세안 후 아무것도 바르지 않았을 때 피부 상태는?",
                        "SCALE",
                        Arrays.asList(
                                new SurveyOptionDto("1", "매우 건조함", 5),
                                new SurveyOptionDto("2", "약간 건조함", 4),
                                new SurveyOptionDto("3", "보통", 3),
                                new SurveyOptionDto("4", "약간 기름짐", 2),
                                new SurveyOptionDto("5", "매우 기름짐", 1)
                        ),
                        true,
                        1
                ),

                new SurveyQuestionDto(
                        "Q002",
                        "하루 종일 피부가 어떤 느낌인가요?",
                        "MULTIPLE_CHOICE",
                        Arrays.asList(
                                new SurveyOptionDto("very_oily", "하루종일 매우 기름짐", 1),
                                new SurveyOptionDto("slightly_oily", "오후부터 기름짐", 2),
                                new SurveyOptionDto("normal", "적당함", 3),
                                new SurveyOptionDto("slightly_dry", "약간 당김", 4),
                                new SurveyOptionDto("very_dry", "하루종일 매우 건조함", 5)
                        ),
                        true,
                        2
                ),

                new SurveyQuestionDto(
                        "Q003",
                        "T존(이마, 코) 부위의 기름기 정도는?",
                        "SCALE",
                        Arrays.asList(
                                new SurveyOptionDto("1", "전혀 기름지지 않음", 5),
                                new SurveyOptionDto("2", "약간 기름짐", 4),
                                new SurveyOptionDto("3", "보통", 3),
                                new SurveyOptionDto("4", "많이 기름짐", 2),
                                new SurveyOptionDto("5", "매우 기름짐", 1)
                        ),
                        true,
                        3
                ),

                new SurveyQuestionDto(
                        "Q004",
                        "모공 크기는 어느 정도인가요?",
                        "MULTIPLE_CHOICE",
                        Arrays.asList(
                                new SurveyOptionDto("very_large", "매우 큼", 1),
                                new SurveyOptionDto("large", "큼", 2),
                                new SurveyOptionDto("medium", "보통", 3),
                                new SurveyOptionDto("small", "작음", 4),
                                new SurveyOptionDto("very_small", "매우 작음", 5)
                        ),
                        true,
                        4
                ),

                new SurveyQuestionDto(
                        "Q005",
                        "트러블(여드름, 뾰루지)이 얼마나 자주 생기나요?",
                        "SCALE",
                        Arrays.asList(
                                new SurveyOptionDto("1", "거의 생기지 않음", 5),
                                new SurveyOptionDto("2", "가끔 생김", 4),
                                new SurveyOptionDto("3", "보통", 3),
                                new SurveyOptionDto("4", "자주 생김", 2),
                                new SurveyOptionDto("5", "항상 생김", 1)
                        ),
                        true,
                        5
                )
        );
    }

    private SurveyRes convertToSurveyRes(Survey survey) {
        return SurveyRes.builder()
                .surveyId(survey.getSurveyId())
                .memberId(survey.getMember().getMemberId())
                .memberName(survey.getMember().getName())
                .skinType(survey.getSkinType())
                .body((Map<String, Object>) survey.getBody())
                .createdAt(survey.getCreatedAt())
                .modifiedAt(survey.getModifiedAt())
                .totalScore(calculateTotalScore((Map<String, Object>) survey.getBody()))
                .recommendation(generateRecommendation(survey.getSkinType()))
                .build();
    }
}
