package syb.moviepedia.tv.dto.response;

import lombok.Builder;

/**
 * TV 홈 화면 인기 TV 목록 응답 클래스
 */
@Builder
public record TVPopularResponse(
        Integer code,
        Integer seasonNum,
        String title,
        String backdrop_path
) {
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/original";
    public TVPopularResponse {
        if (backdrop_path != null) {
            backdrop_path = IMAGE_BASE_URL + backdrop_path;
        }
    }
}
