package hongik.triple.apimodule.application.survey;

import hongik.triple.commonmodule.dto.survey.SurveyOptionDto;
import hongik.triple.commonmodule.dto.survey.SurveyQuestionDto;
import hongik.triple.commonmodule.dto.survey.SurveyReq;
import hongik.triple.commonmodule.dto.survey.SurveyRes;
import hongik.triple.commonmodule.enumerate.SkinType;
import hongik.triple.domainmodule.domain.member.Member;
import hongik.triple.domainmodule.domain.survey.Survey;
import hongik.triple.domainmodule.domain.survey.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyService {

    private final SurveyRepository surveyRepository;

    @Transactional
    public SurveyRes registerSurvey(Member member, SurveyReq request) {
        // Validation
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
                .skinType(SkinType.valueOf(savedSurvey.getSkinType()).getDescription())
                .questions(buildAnsweredQuestions(savedSurvey.getBody()))
                .body(savedSurvey.getBody())
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
        // 부위별 점수 계산
        int tZoneScore = getTZoneScore(answers);           // Q001, Q002 (T존: 이마, 코)
        int uZoneScore = getUZoneScore(answers);           // Q003, Q004 (U존: 턱, 입주변)
        int cheekScore = getCheekScore(answers);           // Q005, Q006 (볼)
        int oilinessScore = getOilinessScore(answers);     // Q007, Q008 (유분/번들거림)
        int drynessScore = getDrynessScore(answers);       // Q009, Q010 (건조함/당김)
        int sensitivityScore = getSensitivityScore(answers); // Q011, Q012 (민감도)

        // 평균 점수 계산
        int totalScore = tZoneScore + uZoneScore + cheekScore + oilinessScore + drynessScore + sensitivityScore;
        double averageScore = totalScore / 6.0;

        log.info("피부 타입 계산 - T존: {}, U존: {}, 볼: {}, 유분: {}, 건조: {}, 민감: {}, 평균: {}",
                tZoneScore, uZoneScore, cheekScore, oilinessScore, drynessScore, sensitivityScore, averageScore);

        // 피부 타입 판별 로직

        // 1. 지성 피부 (OILY)
        if (oilinessScore >= 8 && drynessScore <= 4) {
            return SkinType.OILY;
        }

        if (tZoneScore >= 7 && uZoneScore >= 7 && oilinessScore >= 7) {
            return SkinType.OILY;
        }

        // 2. 건성 피부 (DRY)
        if (drynessScore >= 8 && oilinessScore <= 4) {
            return SkinType.DRY;
        }

        if (cheekScore <= 4 && uZoneScore <= 4 && drynessScore >= 7) {
            return SkinType.DRY;
        }

        // 3. 복합성 피부 (COMBINATION)
        if (Math.abs(tZoneScore - uZoneScore) >= 3) {
            return SkinType.COMBINATION;
        }

        if (Math.abs(tZoneScore - cheekScore) >= 3) {
            return SkinType.COMBINATION;
        }

        if (tZoneScore >= 7 && oilinessScore >= 6 && drynessScore >= 6) {
            return SkinType.COMBINATION;
        }

        if (oilinessScore >= 6 && drynessScore >= 6) {
            return SkinType.COMBINATION;
        }

        // 4. 평균 점수 기반 판별
        if (averageScore >= 7.5) {
            return SkinType.OILY;
        } else if (averageScore <= 4.0) {
            return SkinType.DRY;
        } else {
            return SkinType.COMBINATION;
        }
    }

    private int calculateQuestionScore(Object answer) {
        try {
            int score = Integer.parseInt(answer.toString());
            return Math.min(Math.max(score, 1), 5);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    // T존(이마, 코) 관련 점수
    private int getTZoneScore(Map<String, Object> answers) {
        String[] questions = {"Q001", "Q002"};
        int totalScore = 0;
        for (String questionId : questions) {
            if (answers.containsKey(questionId)) {
                totalScore += calculateQuestionScore(answers.get(questionId));
            }
        }
        return totalScore;
    }

    // U존(턱, 입 주변) 관련 점수
    private int getUZoneScore(Map<String, Object> answers) {
        String[] questions = {"Q003", "Q004"};
        int totalScore = 0;
        for (String questionId : questions) {
            if (answers.containsKey(questionId)) {
                totalScore += calculateQuestionScore(answers.get(questionId));
            }
        }
        return totalScore;
    }

    // 볼 관련 점수
    private int getCheekScore(Map<String, Object> answers) {
        String[] questions = {"Q005", "Q006"};
        int totalScore = 0;
        for (String questionId : questions) {
            if (answers.containsKey(questionId)) {
                totalScore += calculateQuestionScore(answers.get(questionId));
            }
        }
        return totalScore;
    }

    // 유분/번들거림 관련 점수
    private int getOilinessScore(Map<String, Object> answers) {
        String[] questions = {"Q007", "Q008"};
        int totalScore = 0;
        for (String questionId : questions) {
            if (answers.containsKey(questionId)) {
                totalScore += calculateQuestionScore(answers.get(questionId));
            }
        }
        return totalScore;
    }

    // 건조함/당김 관련 점수
    private int getDrynessScore(Map<String, Object> answers) {
        String[] questions = {"Q009", "Q010"};
        int totalScore = 0;
        for (String questionId : questions) {
            if (answers.containsKey(questionId)) {
                totalScore += calculateQuestionScore(answers.get(questionId));
            }
        }
        return totalScore;
    }

    // 민감도 관련 점수
    private int getSensitivityScore(Map<String, Object> answers) {
        String[] questions = {"Q011", "Q012"};
        int totalScore = 0;
        for (String questionId : questions) {
            if (answers.containsKey(questionId)) {
                totalScore += calculateQuestionScore(answers.get(questionId));
            }
        }
        return totalScore;
    }

    private Map<String, Object> processSurveyBody(Map<String, Object> answers) {
        Map<String, Object> processedBody = new HashMap<>();

        // 질문별 답변 저장 (score만 저장)
        for (Map.Entry<String, Object> entry : answers.entrySet()) {
            String questionId = entry.getKey();
            Object answer = entry.getValue();
            int score = calculateQuestionScore(answer);

            processedBody.put(questionId, score);
        }

        // 카테고리별 점수 저장
        Map<String, Object> categoryScores = new HashMap<>();
        categoryScores.put("tZoneScore", getTZoneScore(answers));
        categoryScores.put("uZoneScore", getUZoneScore(answers));
        categoryScores.put("cheekScore", getCheekScore(answers));
        categoryScores.put("oilinessScore", getOilinessScore(answers));
        categoryScores.put("drynessScore", getDrynessScore(answers));
        categoryScores.put("sensitivityScore", getSensitivityScore(answers));

        processedBody.put("categoryScores", categoryScores);

        return processedBody;
    }

    private int calculateTotalScore(Map<String, Object> body) {
        int totalScore = 0;

        for (Map.Entry<String, Object> entry : body.entrySet()) {
            if (entry.getKey().equals("categoryScores")) {
                continue;
            }

            Object value = entry.getValue();
            if (value instanceof Integer) {
                totalScore += (Integer) value;
            }
        }

        return totalScore;
    }

    private String generateRecommendation(SkinType skinType) {
        return switch (skinType) {
            case OILY -> """
                    지성 피부로 판단됩니다.
                    • 가볍고 산뜻한 젤 타입 제품을 사용하세요
                    • 과도한 세안은 피하고, 하루 2회 정도가 적당합니다
                    • 오일프리 제품과 논코메도제닉 제품을 선택하세요
                    • 주 1-2회 각질 제거로 모공 관리를 하세요
                    • 수분 공급도 중요하니 가벼운 보습제를 사용하세요
                    """;
            case DRY -> """
                    건성 피부로 판단됩니다.
                    • 크림 타입의 풍부한 보습 제품을 사용하세요
                    • 순한 클렌징 제품으로 피부 장벽을 보호하세요
                    • 세라마이드, 히알루론산 성분이 함유된 제품이 좋습니다
                    • 각질 제거는 주 1회 이하로 부드럽게 하세요
                    • 충분한 수분 섭취와 실내 습도 유지가 중요합니다
                    """;
            case COMBINATION -> """
                    복합성 피부로 판단됩니다.
                    • T존과 U존을 구분하여 관리하세요
                    • T존은 가볍게, U존과 볼은 충분히 보습하세요
                    • 밸런싱 토너로 피부 균형을 맞추세요
                    • 부위별로 다른 제품을 사용하는 것도 좋은 방법입니다
                    • 계절에 따라 제품을 조절하여 사용하세요
                    """;
        };
    }

    private List<SurveyQuestionDto> buildSurveyQuestions() {
        return Arrays.asList(
                // T존(이마, 코) 관련 문항 (Q001-Q002)
                new SurveyQuestionDto(
                        "Q001",
                        "이마와 코(T존) 부위의 피지 분비량은 어떤가요?",
                        "SCALE",
                        Arrays.asList(
                                new SurveyOptionDto("1", "거의 없음", 1),
                                new SurveyOptionDto("2", "조금 있음", 2),
                                new SurveyOptionDto("3", "보통", 3),
                                new SurveyOptionDto("4", "많은 편", 4),
                                new SurveyOptionDto("5", "매우 많음", 5)
                        ),
                        true,
                        1
                ),

                new SurveyQuestionDto(
                        "Q002",
                        "T존(이마, 코) 부위가 번들거리거나 유분기가 도는 정도는?",
                        "SCALE",
                        Arrays.asList(
                                new SurveyOptionDto("1", "전혀 없음", 1),
                                new SurveyOptionDto("2", "가끔 있음", 2),
                                new SurveyOptionDto("3", "보통", 3),
                                new SurveyOptionDto("4", "자주 있음", 4),
                                new SurveyOptionDto("5", "항상 번들거림", 5)
                        ),
                        true,
                        2
                ),

                // U존(턱, 입 주변) 관련 문항 (Q003-Q004)
                new SurveyQuestionDto(
                        "Q003",
                        "턱과 입 주변(U존) 부위의 피지 분비량은 어떤가요?",
                        "SCALE",
                        Arrays.asList(
                                new SurveyOptionDto("1", "거의 없음", 1),
                                new SurveyOptionDto("2", "조금 있음", 2),
                                new SurveyOptionDto("3", "보통", 3),
                                new SurveyOptionDto("4", "많은 편", 4),
                                new SurveyOptionDto("5", "매우 많음", 5)
                        ),
                        true,
                        3
                ),

                new SurveyQuestionDto(
                        "Q004",
                        "U존(턱, 입 주변) 부위가 번들거리거나 유분기가 도는 정도는?",
                        "SCALE",
                        Arrays.asList(
                                new SurveyOptionDto("1", "전혀 없음", 1),
                                new SurveyOptionDto("2", "가끔 있음", 2),
                                new SurveyOptionDto("3", "보통", 3),
                                new SurveyOptionDto("4", "자주 있음", 4),
                                new SurveyOptionDto("5", "항상 번들거림", 5)
                        ),
                        true,
                        4
                ),

                // 볼 관련 문항 (Q005-Q006)
                new SurveyQuestionDto(
                        "Q005",
                        "볼 부위의 피지 분비량은 어떤가요?",
                        "SCALE",
                        Arrays.asList(
                                new SurveyOptionDto("1", "거의 없음", 1),
                                new SurveyOptionDto("2", "조금 있음", 2),
                                new SurveyOptionDto("3", "보통", 3),
                                new SurveyOptionDto("4", "많은 편", 4),
                                new SurveyOptionDto("5", "매우 많음", 5)
                        ),
                        true,
                        5
                ),

                new SurveyQuestionDto(
                        "Q006",
                        "볼 부위가 번들거리거나 유분기가 도는 정도는?",
                        "SCALE",
                        Arrays.asList(
                                new SurveyOptionDto("1", "전혀 없음", 1),
                                new SurveyOptionDto("2", "가끔 있음", 2),
                                new SurveyOptionDto("3", "보통", 3),
                                new SurveyOptionDto("4", "자주 있음", 4),
                                new SurveyOptionDto("5", "항상 번들거림", 5)
                        ),
                        true,
                        6
                ),

                // 유분/번들거림 관련 문항 (Q007-Q008)
                new SurveyQuestionDto(
                        "Q007",
                        "세안 후 얼마나 빨리 피부가 번들거리기 시작하나요?",
                        "SCALE",
                        Arrays.asList(
                                new SurveyOptionDto("1", "하루 종일 안 그럼", 1),
                                new SurveyOptionDto("2", "저녁 즈음", 2),
                                new SurveyOptionDto("3", "오후쯤", 3),
                                new SurveyOptionDto("4", "점심 전후", 4),
                                new SurveyOptionDto("5", "1-2시간 이내", 5)
                        ),
                        true,
                        7
                ),

                new SurveyQuestionDto(
                        "Q008",
                        "화장이나 선크림이 들뜨거나 무너지는 정도는?",
                        "SCALE",
                        Arrays.asList(
                                new SurveyOptionDto("1", "거의 그대로 유지", 1),
                                new SurveyOptionDto("2", "조금 무너짐", 2),
                                new SurveyOptionDto("3", "보통", 3),
                                new SurveyOptionDto("4", "자주 무너짐", 4),
                                new SurveyOptionDto("5", "매우 심하게 무너짐", 5)
                        ),
                        true,
                        8
                ),

                // 건조함/당김 관련 문항 (Q009-Q010)
                new SurveyQuestionDto(
                        "Q009",
                        "세안 후 피부가 당기는 느낌이 얼마나 드나요?",
                        "SCALE",
                        Arrays.asList(
                                new SurveyOptionDto("1", "전혀 안 당김", 1),
                                new SurveyOptionDto("2", "약간 당김", 2),
                                new SurveyOptionDto("3", "보통", 3),
                                new SurveyOptionDto("4", "많이 당김", 4),
                                new SurveyOptionDto("5", "매우 심하게 당김", 5)
                        ),
                        true,
                        9
                ),

                new SurveyQuestionDto(
                        "Q010",
                        "피부 각질이나 건조함으로 인한 푸석함이 얼마나 있나요?",
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

                // 민감도 관련 문항 (Q011-Q012)
                new SurveyQuestionDto(
                        "Q011",
                        "화장품이나 외부 자극에 피부가 민감하게 반응하나요?",
                        "SCALE",
                        Arrays.asList(
                                new SurveyOptionDto("1", "전혀 민감하지 않음", 1),
                                new SurveyOptionDto("2", "가끔 민감함", 2),
                                new SurveyOptionDto("3", "보통", 3),
                                new SurveyOptionDto("4", "자주 민감함", 4),
                                new SurveyOptionDto("5", "매우 민감함", 5)
                        ),
                        true,
                        11
                ),

                new SurveyQuestionDto(
                        "Q012",
                        "계절이나 환경 변화에 따른 피부 트러블 발생 정도는?",
                        "SCALE",
                        Arrays.asList(
                                new SurveyOptionDto("1", "거의 없음", 1),
                                new SurveyOptionDto("2", "가끔 있음", 2),
                                new SurveyOptionDto("3", "보통", 3),
                                new SurveyOptionDto("4", "자주 있음", 4),
                                new SurveyOptionDto("5", "항상 있음", 5)
                        ),
                        true,
                        12
                )
        );
    }

    /**
     * 저장된 답변과 질문을 결합하여 반환
     */
    private List<SurveyQuestionDto> buildAnsweredQuestions(Map<String, Object> body) {
        List<SurveyQuestionDto> allQuestions = buildSurveyQuestions();

        return allQuestions.stream()
                .map(question -> {
                    String questionId = question.questionId();
                    Object answerValue = body.get(questionId);

                    if (answerValue instanceof Integer) {
                        int score = (Integer) answerValue;

                        // 해당 점수에 맞는 옵션 찾기
                        SurveyOptionDto selectedOption = question.options().stream()
                                .filter(opt -> opt.value() == score)
                                .findFirst()
                                .orElse(null);

                        // 답변이 선택된 질문 반환
                        return new SurveyQuestionDto(
                                question.questionId(),
                                question.questionText(),
                                question.questionType(),
                                question.options(),
                                question.required(),
                                question.order(),
                                score,  // 선택된 답변 점수
                                selectedOption != null ? selectedOption.label() : null  // 선택된 옵션 텍스트
                        );
                    }

                    return question;
                })
                .collect(Collectors.toList());
    }

    private SurveyRes convertToSurveyRes(Survey survey) {
        return SurveyRes.builder()
                .surveyId(survey.getSurveyId())
                .memberId(survey.getMember().getMemberId())
                .memberName(survey.getMember().getName())
                .skinType(SkinType.valueOf(survey.getSkinType()).getDescription())
                .questions(buildAnsweredQuestions(survey.getBody()))
                .body(survey.getBody())
                .createdAt(survey.getCreatedAt())
                .modifiedAt(survey.getModifiedAt())
                .totalScore(calculateTotalScore(survey.getBody()))
                .recommendation(generateRecommendation(SkinType.valueOf(survey.getSkinType())))
                .build();
    }
}