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
        String certification
) {
    public static AllTVsResponse from(TV tv) {
        return AllTVsResponse.builder()
                .code(tv.getSeriesCode())
                .posterPath(tv.getPosterPath())
                .title(tv.getSeries().getTitle())
                .seasonNum(tv.getSeasonNum())
                .certification(tv.getSeries().getCertification())
                .build();
    }
}
