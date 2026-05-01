package syb.moviepedia.movie.external.kobis;

import lombok.Builder;

/**
 * 박스 오피스 정보
 * @param movieName : KMDB api를 가져올 때 사용할 정보
 */
@Builder
public record KobisBoxOfficeInfo(
        int rank,
        String movieName
) {
}
