package syb.moviepedia.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import syb.moviepedia.movie.domain.Movie;

public interface MovieRepository extends JpaRepository<Movie, Long> {
}
