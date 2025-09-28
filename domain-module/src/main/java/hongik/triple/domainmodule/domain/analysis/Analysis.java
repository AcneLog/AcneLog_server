package hongik.triple.domainmodule.domain.analysis;

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
@Table(name = "analysis")
@SQLDelete(sql = "UPDATE analysis SET deleted_at = NOW() where banner_id = ?")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Analysis extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analysis_id")
    private Long analysisId;

    @JoinColumn(name = "member_id") //, nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Column(name = "skin_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SkinType skinType;

    @Builder
    public Analysis(Member member, SkinType skinType) {
        this.member = member;
        this.skinType = skinType;
    }
}
