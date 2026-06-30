package syb.moviepedia.tv.repsitory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import syb.moviepedia.tv.domain.TVSeries;

public interface TVSeriesRepository extends JpaRepository<TVSeries, Long> {
}
