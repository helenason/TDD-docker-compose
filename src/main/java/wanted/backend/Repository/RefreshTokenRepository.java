package wanted.backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wanted.backend.Domain.Member.Member;
import wanted.backend.Domain.Member.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByMember(Member member);

}
