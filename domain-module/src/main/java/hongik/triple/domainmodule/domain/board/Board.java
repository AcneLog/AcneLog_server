package hongik.triple.domainmodule.domain.board;

import hongik.triple.domainmodule.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;

@Entity
@Getter
@Table(name = "board")
@SQLDelete(sql = "UPDATE board SET deleted_at = NOW() where banner_id = ?")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Board extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long boardId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 수정 메서드
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    @Builder
    public Board(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
