package syb.moviepedia.jwt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import syb.moviepedia.jwt.domain.JwtRefresh;

import java.time.LocalDateTime;

public interface JwtRepository extends JpaRepository<JwtRefresh, Long> {
    Boolean existsByRefreshToken(String refreshToken);

    void deleteByRefreshToken(String refresh);

    void deleteByLoginId(String loginId);

    @Transactional
    void deleteByCreatedDateBefore(LocalDateTime cutoff);
}
