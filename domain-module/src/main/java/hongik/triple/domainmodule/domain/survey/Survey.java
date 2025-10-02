package hongik.triple.domainmodule.domain.survey;

import hongik.triple.commonmodule.enumerate.AcneType;
import hongik.triple.commonmodule.enumerate.SkinType;
import hongik.triple.domainmodule.common.BaseTimeEntity;
import hongik.triple.domainmodule.domain.member.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;

@Entity
@Getter
@Table(name = "survey")
@SQLDelete(sql = "UPDATE survey SET deleted_at = NOW() where banner_id = ?")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Survey extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "survey_id")
    private Long surveyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "body", columnDefinition = "json", nullable = false)
    private Object body;


    @Enumerated(EnumType.STRING)
    @Column(name = "skin_type", nullable = false)
    // @Enumerated(EnumType.STRING) 사용 X, String 형식으로 저장 (이유: description도 같이 저장되는 것을 방지하기 위해)
    private String skinType;

    @Builder
    public Survey(Member member, Object body, SkinType skinType) {
        this.member = member;
        this.body = body;
        this.skinType = skinType.name();
    }
}
