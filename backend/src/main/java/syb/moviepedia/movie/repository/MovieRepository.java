package syb.moviepedia.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import syb.moviepedia.movie.domain.Movie;

import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    Optional<Movie> findByMovieId(Long movieId);
    Boolean existsByMovieId(Long movieId);
}
