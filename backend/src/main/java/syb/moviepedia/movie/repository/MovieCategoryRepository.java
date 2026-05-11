package syb.moviepedia.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import syb.moviepedia.common.MovieCategoryType;
import syb.moviepedia.movie.domain.MovieCategory;
import java.util.List;

public interface MovieCategoryRepository extends JpaRepository<MovieCategory, Long> {
    List<MovieCategory> findByCategoryTypeOrderByPopularityDesc(MovieCategoryType movieCategoryType);
    void deleteByCategoryType(MovieCategoryType type);
}
