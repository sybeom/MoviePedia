package syb.moviepedia.movie.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import syb.moviepedia.movie.domain.Country;

import java.util.List;

public interface CountryRepository extends JpaRepository<Country, Long> {

    @Query("select c.name from Country c where c.code in :codes")
    List<String> findNameByCodeIn(@Param("codes") List<String> codes);
}
