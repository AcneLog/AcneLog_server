package hongik.triple.domainmodule.domain.member;

import hongik.triple.domainmodule.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;

@Entity
@Getter
@Table(name = "member")
@SQLDelete(sql = "UPDATE banner SET deleted_at = NOW() where banner_id = ?")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;

    public Member(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
}
