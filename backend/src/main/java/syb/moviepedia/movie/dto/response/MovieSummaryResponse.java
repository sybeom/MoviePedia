package syb.moviepedia.movie.dto.response;

import lombok.Builder;
import syb.moviepedia.common.MovieGenre;
import syb.moviepedia.movie.domain.Movie;
import syb.moviepedia.movie.domain.MovieCategory;

import java.util.List;

/**
 * 홈 화면 카테고리 영화 응답 DTO
 */
@Builder
public record MovieSummaryResponse(
        Long code,
        String title, // 제목
        String poster, // 포스터
        List<String> genre, // 장르
        String certification
) {
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/";
    private static String SIZE = "original";

    public MovieSummaryResponse {
        if (poster != null && !poster.isBlank()) {
            poster = IMAGE_BASE_URL + SIZE + poster;
        }
    }

    public static MovieSummaryResponse from(Movie m) {
        return MovieSummaryResponse.builder()
                .code(m.getCode())
                .title(m.getTitle())
                .poster(m.getPosterPath())
                .genre(m.getGenreNames())
                .certification(m.getCertification())
                .build();
    }
}
