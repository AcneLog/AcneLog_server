package hongik.triple.apimodule.board;

import hongik.triple.apimodule.application.board.BoardService;
import hongik.triple.commonmodule.dto.board.BoardReq;
import hongik.triple.commonmodule.dto.board.BoardRes;
import hongik.triple.domainmodule.domain.board.Board;
import hongik.triple.domainmodule.domain.board.repository.BoardRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@DisplayName("BoardService 테스트")
@ExtendWith(MockitoExtension.class)
public class BoardServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @InjectMocks
    private BoardService boardService;

    @Nested
    @DisplayName("공지사항 등록")
    class RegisterBoard {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            Long boardId = 1L;
            BoardReq request = new BoardReq("테스트 제목", "테스트 내용");

            Board board = Board.builder()
                    .title("테스트 제목")
                    .content("테스트 내용")
                    .build();
            ReflectionTestUtils.setField(board, "boardId", boardId);

            given(boardRepository.save(any(Board.class))).willReturn(board);

            // when
            BoardRes response = boardService.registerBoard(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.boardId()).isEqualTo(1L);
            assertThat(response.title()).isEqualTo("테스트 제목");
            assertThat(response.content()).isEqualTo("테스트 내용");

            verify(boardRepository, times(1)).save(any(Board.class));
        }

        @Test
        @DisplayName("실패 - 제목이 null")
        void fail_TitleIsNull() {
            // given
            BoardReq request = new BoardReq(null, "테스트 내용");

            // when & then
            assertThatThrownBy(() -> boardService.registerBoard(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Title cannot be empty");

            verify(boardRepository, never()).save(any(Board.class));
        }

        @Test
        @DisplayName("실패 - 제목이 빈 문자열")
        void fail_TitleIsEmpty() {
            // given
            BoardReq request = new BoardReq("   ", "테스트 내용");

            // when & then
            assertThatThrownBy(() -> boardService.registerBoard(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Title cannot be empty");

            verify(boardRepository, never()).save(any(Board.class));
        }

        @Test
        @DisplayName("실패 - 내용이 null")
        void fail_ContentIsNull() {
            // given
            BoardReq request = new BoardReq("테스트 제목", null);

            // when & then
            assertThatThrownBy(() -> boardService.registerBoard(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Content cannot be empty");

            verify(boardRepository, never()).save(any(Board.class));
        }

        @Test
        @DisplayName("실패 - 내용이 빈 문자열")
        void fail_ContentIsEmpty() {
            // given
            BoardReq request = new BoardReq("테스트 제목", "   ");

            // when & then
            assertThatThrownBy(() -> boardService.registerBoard(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Content cannot be empty");

            verify(boardRepository, never()).save(any(Board.class));
        }
    }

    @Nested
    @DisplayName("공지사항 수정")
    class UpdateBoard {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            Long boardId = 1L;
            BoardReq request = new BoardReq("수정된 제목", "수정된 내용");

            Board board = Board.builder()
                    .title("원래 제목")
                    .content("원래 내용")
                    .build();
            ReflectionTestUtils.setField(board, "boardId", boardId);

            given(boardRepository.findById(boardId)).willReturn(Optional.of(board));

            // when
            BoardRes response = boardService.updateBoard(boardId, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.boardId()).isEqualTo(boardId);
            assertThat(response.title()).isEqualTo("수정된 제목");
            assertThat(response.content()).isEqualTo("수정된 내용");

            verify(boardRepository, times(1)).findById(boardId);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 게시글")
        void fail_NotFound() {
            // given
            Long boardId = 999L;
            BoardReq request = new BoardReq("수정된 제목", "수정된 내용");

            given(boardRepository.findById(boardId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> boardService.updateBoard(boardId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Board not found with id: " + boardId);

            verify(boardRepository, times(1)).findById(boardId);
        }

        @Test
        @DisplayName("실패 - 제목이 null")
        void fail_TitleIsNull() {
            // given
            Long boardId = 1L;
            BoardReq request = new BoardReq(null, "수정된 내용");

            Board board = Board.builder()
                    .title("원래 제목")
                    .content("원래 내용")
                    .build();
            ReflectionTestUtils.setField(board, "boardId", boardId);

            given(boardRepository.findById(boardId)).willReturn(Optional.of(board));

            // when & then
            assertThatThrownBy(() -> boardService.updateBoard(boardId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Title cannot be empty");

            verify(boardRepository, times(1)).findById(boardId);
        }

        @Test
        @DisplayName("실패 - 내용이 null")
        void fail_ContentIsNull() {
            // given
            Long boardId = 1L;
            BoardReq request = new BoardReq("수정된 제목", null);

            Board board = Board.builder()
                    .title("원래 제목")
                    .content("원래 내용")
                    .build();
            ReflectionTestUtils.setField(board, "boardId", boardId);

            given(boardRepository.findById(boardId)).willReturn(Optional.of(board));

            // when & then
            assertThatThrownBy(() -> boardService.updateBoard(boardId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Content cannot be empty");

            verify(boardRepository, times(1)).findById(boardId);
        }
    }

    @Nested
    @DisplayName("공지사항 삭제")
    class DeleteBoard {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            Long boardId = 1L;

            Board board = Board.builder()
                    .title("테스트 제목")
                    .content("테스트 내용")
                    .build();
            ReflectionTestUtils.setField(board, "boardId", boardId);

            given(boardRepository.findById(boardId)).willReturn(Optional.of(board));
            doNothing().when(boardRepository).delete(board);

            // when
            boardService.deleteBoard(boardId);

            // then
            verify(boardRepository, times(1)).findById(boardId);
            verify(boardRepository, times(1)).delete(board);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 게시글")
        void fail_NotFound() {
            // given
            Long boardId = 999L;

            given(boardRepository.findById(boardId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> boardService.deleteBoard(boardId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Board not found with id: " + boardId);

            verify(boardRepository, times(1)).findById(boardId);
            verify(boardRepository, never()).delete(any(Board.class));
        }
    }

    @Nested
    @DisplayName("공지사항 단일 조회")
    class GetBoard {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            Long boardId = 1L;

            Board board = Board.builder()
                    .title("테스트 제목")
                    .content("테스트 내용")
                    .build();
            ReflectionTestUtils.setField(board, "boardId", boardId);

            given(boardRepository.findById(boardId)).willReturn(Optional.of(board));

            // when
            BoardRes response = boardService.getBoard(boardId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.boardId()).isEqualTo(boardId);
            assertThat(response.title()).isEqualTo("테스트 제목");
            assertThat(response.content()).isEqualTo("테스트 내용");

            verify(boardRepository, times(1)).findById(boardId);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 게시글")
        void fail_NotFound() {
            // given
            Long boardId = 999L;

            given(boardRepository.findById(boardId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> boardService.getBoard(boardId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Board not found with id: " + boardId);

            verify(boardRepository, times(1)).findById(boardId);
        }
    }

    @Nested
    @DisplayName("공지사항 목록 조회")
    class GetBoardList {

        @Test
        @DisplayName("성공 - 데이터가 있는 경우")
        void success_WithData() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            Board board1 = Board.builder()
                    .title("제목1")
                    .content("내용1")
                    .build();
            ReflectionTestUtils.setField(board1, "boardId", 1L);

            Board board2 = Board.builder()
                    .title("제목2")
                    .content("내용2")
                    .build();
            ReflectionTestUtils.setField(board2, "boardId", 2L);

            List<Board> boards = List.of(board1, board2);
            Page<Board> boardPage = new PageImpl<>(boards, pageable, boards.size());

            given(boardRepository.findAllByOrderByCreatedAtDesc(pageable)).willReturn(boardPage);

            // when
            Page<BoardRes> response = boardService.getBoardList(pageable);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getContent().get(0).boardId()).isEqualTo(1L);
            assertThat(response.getContent().get(0).title()).isEqualTo("제목1");
            assertThat(response.getContent().get(1).boardId()).isEqualTo(2L);
            assertThat(response.getContent().get(1).title()).isEqualTo("제목2");
            assertThat(response.getTotalElements()).isEqualTo(2);

            verify(boardRepository, times(1)).findAllByOrderByCreatedAtDesc(pageable);
        }

        @Test
        @DisplayName("성공 - 데이터가 없는 경우")
        void success_NoData() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            Page<Board> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            given(boardRepository.findAllByOrderByCreatedAtDesc(pageable)).willReturn(emptyPage);

            // when
            Page<BoardRes> response = boardService.getBoardList(pageable);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).isEmpty();
            assertThat(response.getTotalElements()).isEqualTo(0);

            verify(boardRepository, times(1)).findAllByOrderByCreatedAtDesc(pageable);
        }

        @Test
        @DisplayName("성공 - 페이지네이션 확인")
        void success_Pagination() {
            // given
            Pageable pageable = PageRequest.of(1, 5); // 2번째 페이지, 5개씩

            Board board = Board.builder()
                    .title("제목6")
                    .content("내용6")
                    .build();
            ReflectionTestUtils.setField(board, "boardId", 6L);

            List<Board> boards = List.of(board);
            Page<Board> boardPage = new PageImpl<>(boards, pageable, 10); // 전체 10개

            given(boardRepository.findAllByOrderByCreatedAtDesc(pageable)).willReturn(boardPage);

            // when
            Page<BoardRes> response = boardService.getBoardList(pageable);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getTotalElements()).isEqualTo(10);
            assertThat(response.getTotalPages()).isEqualTo(2);
            assertThat(response.getNumber()).isEqualTo(1); // 현재 페이지

            verify(boardRepository, times(1)).findAllByOrderByCreatedAtDesc(pageable);
        }
    }
}
