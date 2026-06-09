package syb.moviepedia.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import syb.moviepedia.movie.domain.Movie;
import syb.moviepedia.movie.domain.Video;

import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long> {

    boolean existsByMovie(Movie movie);
    List<Video> findByMovie(Movie movie);
}
