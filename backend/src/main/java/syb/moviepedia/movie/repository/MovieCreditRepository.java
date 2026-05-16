package syb.moviepedia.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import syb.moviepedia.movie.domain.Cast;
import syb.moviepedia.movie.domain.Credit;

import java.util.List;

public interface MovieCreditRepository extends JpaRepository<Credit, Long> {
    List<Credit> findByMovieId(Long movieId);

    List<Cast> findByMovieIdOrderByCastOrderAsc(Long movieId);

}
