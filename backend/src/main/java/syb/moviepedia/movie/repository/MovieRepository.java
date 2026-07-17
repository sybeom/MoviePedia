package syb.moviepedia.movie.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import syb.moviepedia.movie.domain.Movie;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    Optional<Movie> findByCode(Integer code);

    @Query("""
        select m
        from Movie m
    """)
    Slice<Movie> findAllMovies(Pageable pageable);


    // codes에 존재하는 영화 코드들 찾기
    @Query("""
        select m.code
        from Movie m
        where m.code in :codes
    """)
    Set<Long> findCodesByCodeIn(@Param("codes") List<Integer> codes);

    // codes에 존재하는 영화 코드들 찾기
    @Query("""
        select m
        from Movie m
        where m.code in :codes
    """)
    List<Movie> findMoviesByCodeIn(@Param("codes") List<Integer> codes);

    // id가 마지막 조회 id보다 큰 영화 중에서 id 오름차순으로 최대 1000개 조회
    List<Movie> findTop1000ByIdGreaterThanOrderByIdAsc(Long id);

    List<Movie> findAllByCodeIn(Collection<Integer> codes);
}
