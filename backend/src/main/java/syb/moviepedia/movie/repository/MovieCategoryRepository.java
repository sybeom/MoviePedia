package syb.moviepedia.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import syb.moviepedia.common.MovieCategoryType;
import syb.moviepedia.movie.domain.MovieCategory;

public interface MovieCategoryRepository extends JpaRepository<MovieCategory, Long> {
    void deleteByCategoryType(MovieCategoryType type);
}
