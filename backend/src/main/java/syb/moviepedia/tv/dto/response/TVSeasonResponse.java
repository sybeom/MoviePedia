package syb.moviepedia.tv.dto.response;

import lombok.Builder;
import syb.moviepedia.movie.domain.Credit;

import java.time.LocalDate;
import java.util.List;

@Builder
public record TVSeasonResponse(
        Integer seasonCode,
        String title,
        List<String> genre,
        List<String> country,
        Integer runtime,
        LocalDate releaseDate,
        String certification,
        String posterPath,
        String overview,
        List<Credit> credit
) {
}
