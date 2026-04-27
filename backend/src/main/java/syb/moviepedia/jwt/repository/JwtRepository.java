package syb.moviepedia.jwt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import syb.moviepedia.jwt.domain.JwtRefresh;

import java.time.LocalDateTime;

public interface JwtRepository extends JpaRepository<JwtRefresh, Long> {
    Boolean existsByRefreshToken(String refreshToken);

    // TODO: Transactional 수정하기 여기가 아니라 서비스에 있어야하는거아닌가
    @Transactional
    void deleteByRefreshToken(String refresh);

    @Transactional
    void deleteByLoginId(String loginId);

    @Transactional
    void deleteByCreatedDateBefore(LocalDateTime cutoff);
}
