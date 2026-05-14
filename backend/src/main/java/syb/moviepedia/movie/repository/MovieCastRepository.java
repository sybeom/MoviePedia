package syb.moviepedia.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import syb.moviepedia.movie.domain.Cast;

import java.util.List;

public interface MovieCastRepository extends JpaRepository<Cast, Long> {
    List<Cast> findByMovieId(Integer movieId);

    List<Cast> findByMovieIdOrderByCastOrderAsc(Long movieId);

}
