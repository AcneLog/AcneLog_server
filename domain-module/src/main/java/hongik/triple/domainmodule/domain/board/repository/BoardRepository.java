package hongik.triple.domainmodule.domain.board.repository;

import hongik.triple.domainmodule.domain.board.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    // 페이지네이션 조회 (최신순)
    Page<Board> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
