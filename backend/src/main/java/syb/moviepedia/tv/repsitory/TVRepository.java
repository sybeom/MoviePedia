package syb.moviepedia.tv.repsitory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import syb.moviepedia.movie.domain.Movie;
import syb.moviepedia.tv.domain.TV;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TVRepository extends JpaRepository<TV, Integer> {

    @Query("""
        select tv1
        from TV tv1
        where tv1.seriesCode in :seriesIds and tv1.seasonNum = (
            select max(tv2.seasonNum)
            from TV tv2
            where tv2.seriesCode=tv1.seriesCode
        )
    """)
    List<TV> findByPopularSeason(Set<Integer> seriesIds);


    @Query("""
        select t
        from TV t
        join fetch t.series
        where t.series.code=:seriesCode and t.seasonNum=:seasonNum
    """)
    Optional<TV> findBySeriesCodeAndSeasonNum(@Param("seriesCode") Integer seriesCode, @Param("seasonNum") Integer seasonNum);


    // id가 마지막 조회 id보다 큰 영화 중에서 id 오름차순으로 최대 1000개 조회
    List<TV> findTop1000ByIdGreaterThanOrderByIdAsc(Long id);
}
