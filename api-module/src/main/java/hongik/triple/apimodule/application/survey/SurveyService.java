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
        member.updateSkinType(skinType.name()); // 유저 피부타입 세팅

        // Response
        return SurveyRes.builder()
                .surveyId(savedSurvey.getSurveyId())
                .memberId(savedSurvey.getMember().getMemberId())
                .memberName(savedSurvey.getMember().getName())
                .skinType(SkinType.valueOf(savedSurvey.getSkinType()))
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

    public Page<SurveyRes> getSurveyList(Member member, Pageable pageable) {
        // Validation

        // Business Logic
        Page<Survey> surveys = (member.getMemberId() != null)
                ? surveyRepository.findByMember_MemberIdOrderByCreatedAtDesc(member.getMemberId(), pageable)
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

        // 12개 필수 문항 확인
        for (SurveyQuestionDto question : questions) {
            if (question.required() && !answers.containsKey(question.questionId())) {
                throw new IllegalArgumentException("필수 질문에 대한 답변이 없습니다: " + question.questionText());
            }
        }

        // 점수 범위 검증 (1-5)
        for (Map.Entry<String, Object> entry : answers.entrySet()) {
            try {
                int score = Integer.parseInt(entry.getValue().toString());
                if (score < 1 || score > 5) {
                    throw new IllegalArgumentException("점수는 1-5 범위여야 합니다: " + entry.getKey());
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("유효하지 않은 점수 형식입니다: " + entry.getKey());
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
        int comedoneScore = getComedoneRelatedScore(answers); // Q001, Q002, Q005, Q006
        int inflammationScore = getInflammationRelatedScore(answers); // Q003, Q004, Q007, Q008
        int pustuleScore = getPustuleRelatedScore(answers); // Q009, Q010
        int folliculitisScore = getFolliculitisRelatedScore(answers); // Q011, Q012

        // 점수 기반 피부 타입 결정 (심각도 순으로 우선순위)
        if (averageScore <= 2.0) {
            return SkinType.OILY;
        } else if (folliculitisScore >= 8) { // 모낭염 문항 평균 4점 이상
            return SkinType.OILY;
        } else if (pustuleScore >= 8) { // 화농성 문항 평균 4점 이상
            return SkinType.OILY;
        } else if (inflammationScore >= 15) { // 염증성 문항 평균 3.75점 이상
            return SkinType.OILY;
        } else if (comedoneScore >= 12) { // 좁쌀 문항 평균 3점 이상
            return SkinType.OILY;
        } else {
            return SkinType.OILY;
        }
    }

    private int calculateQuestionScore(String questionId, Object answer) {
        // 모든 질문이 1-5 척도로 통일
        try {
            int score = Integer.parseInt(answer.toString());
            return Math.min(Math.max(score, 1), 5);
        } catch (NumberFormatException e) {
            return 1; // 기본값
        }
    }

    private int getComedoneRelatedScore(Map<String, Object> answers) {
        // 좁쌀여드름 관련 문항 (Q001, Q002, Q005, Q006)
        String[] comedoneQuestions = {"Q001", "Q002", "Q005", "Q006"};
        int totalScore = 0;

        for (String questionId : comedoneQuestions) {
            if (answers.containsKey(questionId)) {
                totalScore += calculateQuestionScore(questionId, answers.get(questionId));
            }
        }
        return totalScore;
    }

    private int getInflammationRelatedScore(Map<String, Object> answers) {
        // 염증성 여드름 관련 문항 (Q003, Q004, Q007, Q008)
        String[] inflammationQuestions = {"Q003", "Q004", "Q007", "Q008"};
        int totalScore = 0;

        for (String questionId : inflammationQuestions) {
            if (answers.containsKey(questionId)) {
                totalScore += calculateQuestionScore(questionId, answers.get(questionId));
            }
        }
        return totalScore;
    }

    private int getPustuleRelatedScore(Map<String, Object> answers) {
        // 화농성 여드름 관련 문항 (Q009, Q010)
        String[] pustuleQuestions = {"Q009", "Q010"};
        int totalScore = 0;

        for (String questionId : pustuleQuestions) {
            if (answers.containsKey(questionId)) {
                totalScore += calculateQuestionScore(questionId, answers.get(questionId));
            }
        }
        return totalScore;
    }

    private int getFolliculitisRelatedScore(Map<String, Object> answers) {
        // 모낭염 관련 문항 (Q011, Q012)
        String[] folliculitisQuestions = {"Q011", "Q012"};
        int totalScore = 0;

        for (String questionId : folliculitisQuestions) {
            if (answers.containsKey(questionId)) {
                totalScore += calculateQuestionScore(questionId, answers.get(questionId));
            }
        }
        return totalScore;
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

        // 카테고리별 점수도 함께 저장
        Map<String, Object> categoryScores = new HashMap<>();
        categoryScores.put("comedoneScore", getComedoneRelatedScore(answers));
        categoryScores.put("inflammationScore", getInflammationRelatedScore(answers));
        categoryScores.put("pustuleScore", getPustuleRelatedScore(answers));
        categoryScores.put("folliculitisScore", getFolliculitisRelatedScore(answers));

        processedBody.put("categoryScores", categoryScores);

        return processedBody;
    }

    private int calculateTotalScore(Map<String, Object> body) {
        int totalScore = 0;

        for (Map.Entry<String, Object> entry : body.entrySet()) {
            // categoryScores는 제외
            if (entry.getKey().equals("categoryScores")) {
                continue;
            }

            Object value = entry.getValue();
            if (value instanceof Map) {
                Map<String, Object> questionResult = (Map<String, Object>) value;
                Object scoreObj = questionResult.get("score");
                if (scoreObj instanceof Integer) {
                    totalScore += (Integer) scoreObj;
                }
            }
        }

        return totalScore;
    }

    private String generateRecommendation(SkinType skinType) {
        switch (skinType) {
            case OILY:
                return "현재 피부 상태가 양호합니다. 기본적인 세안과 보습 관리를 지속하시고, 자외선 차단제를 꾸준히 사용하세요.";
            case COMBINATION:
                return "좁쌀여드름이 있습니다. BHA나 살리실산 성분의 각질 제거 제품을 사용하고, 논코메도제닉 제품으로 모공 관리에 집중하세요.";
            case DRY:
                return "화농성 여드름이 있습니다. 벤조일 퍼옥사이드나 항생제 성분이 포함된 제품을 사용하고, 피부과 전문의 상담을 받아보세요.";
            default:
                return "정확한 진단을 위해 피부과 전문의와 상담을 받아보세요.";
        }
    }

    private List<SurveyQuestionDto> buildSurveyQuestions() {
        return Arrays.asList(
                // 좁쌀여드름 관련 문항 (Q001-Q002, Q005-Q006)
                new SurveyQuestionDto(
                        "Q001",
                        "얼굴에 작고 하얀 좁쌀 같은 것들이 얼마나 많이 있나요?",
                        "SCALE",
                        Arrays.asList(
                                new SurveyOptionDto("1", "전혀 없음", 1),
                                new SurveyOptionDto("2", "가끔 보임", 2),
                                new SurveyOptionDto("3", "보통", 3),
                                new SurveyOptionDto("4", "자주 보임", 4),
                                new SurveyOptionDto("5", "매우 많음", 5)
                        ),
                        true,
                        1
                ),

                new SurveyQuestionDto(
                        "Q002",
                        "T존(이마, 코) 부위에 블랙헤드나 화이트헤드가 얼마나 많나요?",
                        "SCALE",
                        Arrays.asList(
                                new SurveyOptionDto("1", "전혀 없음", 1),
                                new SurveyOptionDto("2", "조금 있음", 2),
                                new SurveyOptionDto("3", "보통", 3),
                                new SurveyOptionDto("4", "많이 있음", 4),
                                new SurveyOptionDto("5", "매우 많음", 5)
                        ),
                        true,
                        2
                ),

                // 염증성여드름 관련 문항 (Q003-Q004, Q007-Q008)
                new SurveyQuestionDto(
                        "Q003",
                        "빨갛고 부어오른 여드름이 얼마나 자주 생기나요?",
                        "SCALE",
                        Arrays.asList(
                                new SurveyOptionDto("1", "거의 생기지 않음", 1),
                                new SurveyOptionDto("2", "가끔 생김", 2),
                                new SurveyOptionDto("3", "보통", 3),
                                new SurveyOptionDto("4", "자주 생김", 4),
                                new SurveyOptionDto("5", "항상 있음", 5)
                        ),
                        true,
                        3
                ),

                new SurveyQuestionDto(
                        "Q004",
                        "여드름 부위에 통증이나 압통이 얼마나 심한가요?",
                        "SCALE",
                        Arrays.asList(
                                new SurveyOptionDto("1", "전혀 아프지 않음", 1),
                                new SurveyOptionDto("2", "살짝 아픔", 2),
                                new SurveyOptionDto("3", "보통", 3),
                                new SurveyOptionDto("4", "많이 아픔", 4),
                                new SurveyOptionDto("5", "매우 아픔", 5)
                        ),
                        true,
                        4
                ),

                new SurveyQuestionDto(
                        "Q005",
                        "턱이나 입 주변에 작은 돌기들이 얼마나 많이 있나요?",
                        "SCALE",
                        Arrays.asList(
                                new SurveyOptionDto("1", "전혀 없음", 1),
                                new SurveyOptionDto("2", "조금 있음", 2),
                                new SurveyOptionDto("3", "보통", 3),
                                new SurveyOptionDto("4", "많이 있음", 4),
                                new SurveyOptionDto("5", "매우 많음", 5)
                        ),
                        true,
                        5
                ),

                new SurveyQuestionDto(
                        "Q006",
                        "모공이 막힌 느낌이나 피부가 거칠어진 느낌이 얼마나 드나요?",
                        "SCALE",
                        Arrays.asList(
                                new SurveyOptionDto("1", "전혀 없음", 1),
                                new SurveyOptionDto("2", "가끔 있음", 2),
                                new SurveyOptionDto("3", "보통", 3),
                                new SurveyOptionDto("4", "자주 있음", 4),
                                new SurveyOptionDto("5", "항상 있음", 5)
                        ),
                        true,
                        6
                ),

                new SurveyQuestionDto(
                        "Q007",
                        "여드름이 생긴 후 자국이나 흉터가 남는 정도는?",
                        "SCALE",
                        Arrays.asList(
                                new SurveyOptionDto("1", "전혀 남지 않음", 1),
                                new SurveyOptionDto("2", "가끔 남음", 2),
                                new SurveyOptionDto("3", "보통", 3),
                                new SurveyOptionDto("4", "자주 남음", 4),
                                new SurveyOptionDto("5", "항상 남음", 5)
                        ),
                        true,
                        7
                ),

                new SurveyQuestionDto(
                        "Q008",
                        "여드름 주변 피부가 빨갛게 되는 정도는?",
                        "SCALE",
                        Arrays.asList(
                                new SurveyOptionDto("1", "전혀 빨갛지 않음", 1),
                                new SurveyOptionDto("2", "약간 빨감", 2),
                                new SurveyOptionDto("3", "보통", 3),
                                new SurveyOptionDto("4", "많이 빨감", 4),
                                new SurveyOptionDto("5", "매우 빨감", 5)
                        ),
                        true,
                        8
                ),

                // 화농성여드름 관련 문항 (Q009-Q010)
                new SurveyQuestionDto(
                        "Q009",
                        "고름이 찬 여드름(노란 고름이 보이는)이 얼마나 자주 생기나요?",
                        "SCALE",
                        Arrays.asList(
                                new SurveyOptionDto("1", "거의 생기지 않음", 1),
                                new SurveyOptionDto("2", "가끔 생김", 2),
                                new SurveyOptionDto("3", "보통", 3),
                                new SurveyOptionDto("4", "자주 생김", 4),
                                new SurveyOptionDto("5", "항상 있음", 5)
                        ),
                        true,
                        9
                ),

                new SurveyQuestionDto(
                        "Q010",
                        "여드름에서 고름이나 분비물이 나오는 경우가 얼마나 많나요?",
                        "SCALE",
                        Arrays.asList(
                                new SurveyOptionDto("1", "전혀 없음", 1),
                                new SurveyOptionDto("2", "가끔 있음", 2),
                                new SurveyOptionDto("3", "보통", 3),
                                new SurveyOptionDto("4", "자주 있음", 4),
                                new SurveyOptionDto("5", "항상 있음", 5)
                        ),
                        true,
                        10
                ),

                // 모낭염 관련 문항 (Q011-Q012)
                new SurveyQuestionDto(
                        "Q011",
                        "털이 있는 부위(턱수염, 구레나룻 등)에 빨간 돌기나 염증이 얼마나 자주 생기나요?",
                        "SCALE",
                        Arrays.asList(
                                new SurveyOptionDto("1", "거의 생기지 않음", 1),
                                new SurveyOptionDto("2", "가끔 생김", 2),
                                new SurveyOptionDto("3", "보통", 3),
                                new SurveyOptionDto("4", "자주 생김", 4),
                                new SurveyOptionDto("5", "항상 있음", 5)
                        ),
                        true,
                        11
                ),

                new SurveyQuestionDto(
                        "Q012",
                        "면도나 제모 후 빨간 반점이나 염증이 생기는 정도는?",
                        "SCALE",
                        Arrays.asList(
                                new SurveyOptionDto("1", "전혀 생기지 않음", 1),
                                new SurveyOptionDto("2", "가끔 생김", 2),
                                new SurveyOptionDto("3", "보통", 3),
                                new SurveyOptionDto("4", "자주 생김", 4),
                                new SurveyOptionDto("5", "항상 생김", 5)
                        ),
                        true,
                        12
                )
        );
    }

    private SurveyRes convertToSurveyRes(Survey survey) {
        return SurveyRes.builder()
                .surveyId(survey.getSurveyId())
                .memberId(survey.getMember().getMemberId())
                .memberName(survey.getMember().getName())
                .skinType(SkinType.valueOf(survey.getSkinType()))
                .body((Map<String, Object>) survey.getBody())
                .createdAt(survey.getCreatedAt())
                .modifiedAt(survey.getModifiedAt())
                .totalScore(calculateTotalScore((Map<String, Object>) survey.getBody()))
                .recommendation(generateRecommendation(SkinType.valueOf(survey.getSkinType())))
                .build();
    }
}
