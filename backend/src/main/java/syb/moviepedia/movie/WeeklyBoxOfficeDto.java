package syb.moviepedia.movie;

import lombok.Builder;

@Builder
public record WeeklyBoxOfficeDto(
        String title,
        String poster,
        String genre,
        String rating,
        String plot,
        String nation
) { }
