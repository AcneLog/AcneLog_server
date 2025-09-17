package hongik.triple.domainmodule.domain.analysis;

import hongik.triple.domainmodule.common.BaseTimeEntity;
import jakarta.persistence.*;
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
}
