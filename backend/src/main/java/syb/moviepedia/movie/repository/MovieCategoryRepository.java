package syb.moviepedia.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import syb.moviepedia.common.MovieCategoryType;
import syb.moviepedia.movie.domain.Movie;
import syb.moviepedia.movie.domain.MovieCategory;
import java.util.List;
import java.util.Optional;

public interface MovieCategoryRepository extends JpaRepository<MovieCategory, Long> {
//    List<MovieCategory> findByCategoryTypeOrderByPopularityDesc(MovieCategoryType movieCategoryType);
    void deleteByCategoryType(MovieCategoryType type);

    @Query("""
        select mc
        from MovieCategory mc
        join fetch mc.movie
        where mc.categoryType = :categoryType
    """)
    List<MovieCategory> findByCategoryTypeOrderByPopularity(@Param("categoryType") MovieCategoryType categoryType);
}
