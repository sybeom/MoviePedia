package syb.moviepedia.tv.repsitory;

import org.springframework.data.jpa.repository.JpaRepository;
import syb.moviepedia.tv.domain.TVCategory;

public interface TVCategoryRepository extends JpaRepository<TVCategory, Integer> {
}
