package syb.moviepedia.tv.repsitory;

import org.springframework.data.jpa.repository.JpaRepository;
import syb.moviepedia.tv.domain.TV;

public interface TVRepository extends JpaRepository<TV, Integer> {

}
