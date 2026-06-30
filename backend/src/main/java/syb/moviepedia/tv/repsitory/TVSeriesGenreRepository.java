package syb.moviepedia.tv.repsitory;

import org.springframework.data.jpa.repository.JpaRepository;
import syb.moviepedia.tv.domain.TVSeriesGenre;

public interface TVSeriesGenreRepository extends JpaRepository<TVSeriesGenre, Integer> {
}
