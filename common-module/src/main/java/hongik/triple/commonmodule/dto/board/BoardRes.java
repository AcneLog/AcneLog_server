package hongik.triple.commonmodule.dto.board;

import java.time.LocalDateTime;

public record BoardRes(
        Long boardId,
        String title,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}