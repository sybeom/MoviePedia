package syb.moviepedia.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import syb.moviepedia.movie.domain.Genre;

public interface GenreRepository extends JpaRepository<Genre,Long> {

    boolean existsByGenreId(Integer genreId);

    @Query("select g.name from Genre g where g.genreId=:genreId")
    String findNameByGenreId(Integer genreId);
}
