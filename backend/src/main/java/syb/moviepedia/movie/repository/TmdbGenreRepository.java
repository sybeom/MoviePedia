package syb.moviepedia.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import syb.moviepedia.movie.domain.TmdbGenre;

public interface TmdbGenreRepository extends JpaRepository<TmdbGenre,Long> {

    public boolean existsByGenreId(Integer genreId);
}
