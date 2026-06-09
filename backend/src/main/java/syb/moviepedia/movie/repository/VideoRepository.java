package syb.moviepedia.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import syb.moviepedia.movie.domain.Trailer;

public interface TrailerRepository extends JpaRepository<Trailer, Long> {
}
