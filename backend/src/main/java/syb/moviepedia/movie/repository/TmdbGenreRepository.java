package syb.moviepedia.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import syb.moviepedia.movie.domain.TmdbGenre;

public interface TmdbGenreRepository extends JpaRepository<TmdbGenre,Long> {

    boolean existsByGenreId(Integer genreId);

    @Query("select g.name from TmdbGenre g where g.genreId=:genreId")
    String findNameByGenreId(Integer genreId);
}
