package syb.moviepedia.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import syb.moviepedia.movie.domain.Video;

public interface VideoRepository extends JpaRepository<Video, Long> {
}
