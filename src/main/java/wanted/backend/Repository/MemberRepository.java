package wanted.backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wanted.backend.Domain.Member.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Boolean existsByEmail(String email);

}
