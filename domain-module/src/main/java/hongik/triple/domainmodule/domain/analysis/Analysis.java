package hongik.triple.domainmodule.domain.analysis;

import hongik.triple.commonmodule.dto.analysis.NaverProductDto;
import hongik.triple.commonmodule.dto.analysis.YoutubeVideoDto;
import hongik.triple.commonmodule.enumerate.AcneType;
import hongik.triple.domainmodule.common.BaseTimeEntity;
import hongik.triple.domainmodule.domain.member.Member;
import hongik.triple.inframodule.youtube.YoutubeClient;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Getter
@Table(name = "analysis")
@SQLDelete(sql = "UPDATE analysis SET deleted_at = NOW() where analysis_id = ?")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Analysis extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analysis_id")
    private Long analysisId;

    @JoinColumn(name = "member_id") //, nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Column(name = "image_url", nullable = false, columnDefinition = "text")
    private String imageUrl;

    @Column(name = "acne_type", nullable = false, length = 20)
    private String acneType; // @Enumerated(EnumType.STRING) 사용 X, String 형식으로 저장

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "video_data", columnDefinition = "json")
    private List<YoutubeVideoDto> videoData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "product_data", columnDefinition = "json")
    private List<NaverProductDto> productData;


    @Builder
    public Analysis(Member member,
                    AcneType acneType,
                    String imageUrl,
                    Boolean isPublic,
                    List<YoutubeVideoDto> videoData,
                    List<NaverProductDto> productData) {
        this.member = member;
        this.acneType = acneType.name();
        this.imageUrl = imageUrl;
        this.isPublic = isPublic;
        this.videoData = videoData;
        this.productData = productData;
    }
}
