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
    private String memberType;
    private String provider;
    private String skinType;

    public void update(String name, String skinType) {
        this.name = name;
        this.skinType = skinType;
    }

    public Member(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
