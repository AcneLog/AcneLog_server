package hongik.triple.apimodule.presentation.board;

import hongik.triple.apimodule.application.board.BoardService;
import hongik.triple.apimodule.global.common.ApplicationResponse;
import hongik.triple.apimodule.global.security.PrincipalDetails;
import hongik.triple.commonmodule.dto.analysis.AnalysisRes;
import hongik.triple.commonmodule.dto.board.BoardReq;
import hongik.triple.commonmodule.dto.board.BoardRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/board")
@RequiredArgsConstructor
@Tag(name = "Board", description = "공지사항 관련 API")
public class BoardController {

    private final BoardService boardService;

    @PostMapping("/register")
    @Operation(summary = "공지사항 등록", description = "공지사항을 등록합니다. (제목, 본문 두 가지 형태만 있으며, 이미지는 등록할 수 없습니다.)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "공지사항 등록 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BoardRes.class))),
            @ApiResponse(responseCode = "400",
                    description = "잘못된 요청"),
            @ApiResponse(responseCode = "500",
                    description = "서버 오류")
    })
    public ApplicationResponse<?> registerBoard(@RequestBody BoardReq request) {
        BoardRes response = boardService.registerBoard(request);
        return ApplicationResponse.ok(response);
    }

    @PutMapping("/{boardId}")
    @Operation(summary = "공지사항 수정", description = "특정 공지사항을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "공지사항 수정 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BoardRes.class))),
            @ApiResponse(responseCode = "404",
                    description = "공지사항을 찾을 수 없음"),
            @ApiResponse(responseCode = "500",
                    description = "서버 오류")
    })
    public ApplicationResponse<?> updateBoard(@PathVariable Long boardId, @RequestBody BoardReq request) {
        BoardRes response = boardService.updateBoard(boardId, request);
        return ApplicationResponse.ok(response);
    }

    @DeleteMapping("/{boardId}")
    @Operation(summary = "공지사항 삭제", description = "특정 공지사항을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "공지사항 삭제 성공"),
            @ApiResponse(responseCode = "404",
                    description = "공지사항을 찾을 수 없음"),
            @ApiResponse(responseCode = "500",
                    description = "서버 오류")
    })
    public ApplicationResponse<?> deleteBoard(@PathVariable Long boardId) {
        boardService.deleteBoard(boardId);
        return ApplicationResponse.ok("공지사항이 삭제되었습니다.");
    }

    @GetMapping("/{boardId}")
    @Operation(summary = "공지사항 단일 조회", description = "특정 공지사항을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "공지사항 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BoardRes.class))),
            @ApiResponse(responseCode = "404",
                    description = "공지사항을 찾을 수 없음"),
            @ApiResponse(responseCode = "500",
                    description = "서버 오류")
    })
    public ApplicationResponse<?> getBoard(@PathVariable Long boardId) {
        BoardRes response = boardService.getBoard(boardId);
        return ApplicationResponse.ok(response);
    }

    @GetMapping("/list")
    @Operation(summary = "공지사항 목록 조회", description = "공지사항 목록을 페이지네이션으로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "공지사항 목록 조회 성공"),
            @ApiResponse(responseCode = "500",
                    description = "서버 오류")
    })
    public ApplicationResponse<?> getBoardList(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<BoardRes> response = boardService.getBoardList(pageable);
        return ApplicationResponse.ok(response);
    }
}
