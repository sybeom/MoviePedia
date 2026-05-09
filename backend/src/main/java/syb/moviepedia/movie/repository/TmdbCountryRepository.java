package syb.moviepedia.movie.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import syb.moviepedia.movie.domain.Country;

public interface TmdbCountryRepository extends JpaRepository<Country, Long> {
}
