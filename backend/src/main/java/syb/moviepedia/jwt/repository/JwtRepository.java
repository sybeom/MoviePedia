package syb.moviepedia.jwt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import syb.moviepedia.jwt.domain.JwtRefresh;

public interface JwtRepository extends JpaRepository<JwtRefresh, Long> {
    Boolean existsByRefresh(String refreshToken);

    @Transactional
    void deleteByRefresh(String refresh);

    @Transactional
    void deleteByUsername(String username);
}
