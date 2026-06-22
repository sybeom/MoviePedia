package syb.moviepedia.movie.dto.response;

import lombok.Builder;

@Builder
public record MovieBannerResponse(
        Integer movieCode,
        String title,
        String backdropPath
) {
}
