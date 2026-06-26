package syb.moviepedia.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import syb.moviepedia.common.MediaType;
import syb.moviepedia.movie.domain.Genre;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface GenreRepository extends JpaRepository<Genre,Long> {

    boolean existsByCode(Integer genreId);

    Optional<Genre> findByCode(Integer code);

    List<Genre> findByCodeIn(Collection<Integer> codes);

    @Query("""
        select g
        from Genre g
        where g.mediaType=:mediaType or g.mediaType=MediaType.BOTH
    """)
    List<Genre> findAllByMediaType(@Param("mediaType") MediaType mediaType);
}
