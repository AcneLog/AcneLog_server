package hongik.triple.apimodule.application.board;

import hongik.triple.commonmodule.dto.board.BoardReq;
import hongik.triple.commonmodule.dto.board.BoardRes;
import hongik.triple.domainmodule.domain.board.Board;
import hongik.triple.domainmodule.domain.board.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;

    /**
     * 공지사항 등록
     */
    @Transactional
    public BoardRes registerBoard(BoardReq request) {
        // Validation
        validateBoardReq(request);

        // Business Logic
        Board board = Board.builder()
                .title(request.title())
                .content(request.content())
                .build();

        Board savedBoard = boardRepository.save(board);

        // Response
        return convertToBoardRes(savedBoard);
    }

    /**
     * 공지사항 수정
     */
    @Transactional
    public BoardRes updateBoard(Long boardId, BoardReq request) {
        // Validation
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("Board not found with id: " + boardId));

        validateBoardReq(request);

        // Business Logic
        board.update(request.title(), request.content());

        // Response
        return convertToBoardRes(board);
    }

    /**
     * 공지사항 삭제
     */
    @Transactional
    public void deleteBoard(Long boardId) {
        // Validation
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("Board not found with id: " + boardId));

        // Business Logic
        boardRepository.delete(board);
    }

    /**
     * 공지사항 단일 조회
     */
    public BoardRes getBoard(Long boardId) {
        // Validation
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("Board not found with id: " + boardId));

        // Response
        return convertToBoardRes(board);
    }

    /**
     * 공지사항 페이지네이션 조회
     */
    public Page<BoardRes> getBoardList(Pageable pageable) {
        // Business Logic
        Page<Board> boardPage = boardRepository.findAllByOrderByCreatedAtDesc(pageable);

        // Response
        return boardPage.map(this::convertToBoardRes);
    }

    /**
     * BoardReq 유효성 검증
     */
    private void validateBoardReq(BoardReq request) {
        if (request.title() == null || request.title().trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (request.content() == null || request.content().trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be empty");
        }
    }

    /**
     * Board 엔티티를 BoardRes Record로 변환
     */
    private BoardRes convertToBoardRes(Board board) {
        return new BoardRes(
                board.getBoardId(),
                board.getTitle(),
                board.getContent(),
                board.getCreatedAt(),
                board.getModifiedAt()
        );
    }
}
