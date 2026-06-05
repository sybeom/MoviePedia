package syb.moviepedia.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import syb.moviepedia.movie.domain.Movie;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    Optional<Movie> findByCode(Long code);
    Boolean existsByCode(Long code);

    // codes에 존재하는 영화 코드들 찾기
    @Query("""
        select m.code
        from Movie m
        where m.code in :codes
    """)
    Set<Long> findCodesByCodeIn(@Param("codes") List<Long> codes);
}
