package syb.moviepedia.movie.dto.response;

import lombok.Builder;

@Builder
public record MovieBannerResponse(
        Long movieCode,
        String title,
        String backdropPath
) {
}
