package syb.moviepedia.tv.dto.response;

import lombok.Builder;
import syb.moviepedia.tv.domain.QTVSeries;
import syb.moviepedia.tv.domain.TV;

@Builder
public record AllTVsResponse(
        Integer code,
        String posterPath,
        String title,
        Integer seasonNum,
        String contentRating
) {
    public static AllTVsResponse from(TV tv) {
        return AllTVsResponse.builder()
                .code(tv.getCode())
                .posterPath(tv.getPosterPath())
                .title(tv.getTitle())
                .seasonNum(tv.getSeasonNum())
                .contentRating(tv.getSeries().getContentRating())
                .build();
    }
}
