package syb.moviepedia.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import syb.moviepedia.common.MediaType;
import syb.moviepedia.movie.domain.Credit;

import java.util.List;

public interface CreditRepository extends JpaRepository<Credit, Long> {
    List<Credit> findByCodeAndMediaType(Integer code, MediaType mediaType);
}
