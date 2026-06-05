package syb.moviepedia.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import syb.moviepedia.movie.domain.Genre;

import java.util.List;
import java.util.Set;

public interface GenreRepository extends JpaRepository<Genre,Long> {

    boolean existsByGenreId(Integer genreId);

    @Query("select g.name from Genre g where g.genreId=:genreId")
    String findNameByGenreId(Integer genreId);

    @Query("select g.name from Genre g where g.genreId in :genreIds")
    List<String> findGenresByGenreIds(@Param("genreIds") List<Integer> genreIds);
}
