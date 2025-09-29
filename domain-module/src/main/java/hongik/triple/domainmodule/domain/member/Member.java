package hongik.triple.domainmodule.domain.member;

import hongik.triple.commonmodule.enumerate.MemberType;
import hongik.triple.commonmodule.enumerate.SkinType;
import hongik.triple.domainmodule.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;

@Entity
@Getter
@Table(name = "member")
@SQLDelete(sql = "UPDATE member SET deleted_at = NOW() where banner_id = ?")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "member_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberType memberType;

    @Column(name = "skin_type")
    private String skinType; // SkinType enum의 값을 문자열로 저장

    public void updateSkinType(String skinType) {
        this.skinType = skinType;
    }

    public Member(String name, String email, MemberType memberType) {
        this.name = name;
        this.email = email;
        this.memberType = memberType;
        this.skinType = "normal"; // 기본값 설정
    }
}
