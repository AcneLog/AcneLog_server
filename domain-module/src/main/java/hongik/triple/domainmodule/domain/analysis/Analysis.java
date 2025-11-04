package hongik.triple.domainmodule.domain.analysis;

import hongik.triple.commonmodule.enumerate.AcneType;
import hongik.triple.domainmodule.common.BaseTimeEntity;
import hongik.triple.domainmodule.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Table(name = "analysis")
@SQLDelete(sql = "UPDATE analysis SET deleted_at = NOW() where banner_id = ?")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Analysis extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analysis_id")
    private Long analysisId;

    @JoinColumn(name = "member_id") //, nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Column(name = "acne_type", nullable = false, length = 20)
    private String acneType; // @Enumerated(EnumType.STRING) 사용 X, String 형식으로 저장

//    @JdbcTypeCode(SqlTypes.JSON)
//    @Column(name = "recommend_youtube", columnDefinition = "json")
//    private RecommendYoutube recommendYoutube;

    @Builder
    public Analysis(Member member, AcneType acneType) {
        this.member = member;
        this.acneType = acneType.name();
    }
}
