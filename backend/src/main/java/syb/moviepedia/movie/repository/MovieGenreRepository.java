package syb.moviepedia.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import syb.moviepedia.movie.domain.Movie;
import syb.moviepedia.movie.domain.MovieGenre;

import java.util.Optional;

public interface MovieGenreRepository extends JpaRepository<MovieGenre, Long> {
    @Query("""
        select mg
        from MovieGenre mg
        join fetch mg.movie
        where mg.movie.code=:movieCode
    """)
    Optional<MovieGenre> findByMovieCode(@Param("movieCode") Long mvCode);
}
