package wanted.backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wanted.backend.Domain.Board.Board;

public interface BoardRepository extends JpaRepository<Board, Long> {

}
