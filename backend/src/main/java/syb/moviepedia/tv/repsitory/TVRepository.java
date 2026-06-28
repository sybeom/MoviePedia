package syb.moviepedia.tv.repsitory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import syb.moviepedia.tv.domain.TV;

import java.util.List;
import java.util.Set;

public interface TVRepository extends JpaRepository<TV, Integer> {

    @Query("""
        select tv1
        from TV tv1
        where tv1.code in :seriesIds and tv1.seasonNum = (
            select max(tv2.seasonNum)
            from TV tv2
            where tv2.code=tv1.code
        )
    """)
    List<TV> findByPopularSeason(Set<Integer> seriesIds);
}
