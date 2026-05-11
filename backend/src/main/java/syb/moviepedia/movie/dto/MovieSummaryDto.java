package syb.moviepedia.movie.dto;

import lombok.Builder;

import java.util.List;

/**
 * 홈 화면 카테고리 영화
 */
@Builder
public record MovieSummaryDto(
        Long movieCode,
        String title, // 제목
        String poster, // 포스터
        List<String> genre, // 장르
        String certification
) {
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/";
    private static String SIZE = "original";

    public MovieSummaryDto {
        if (poster != null && !poster.isBlank()) {
            poster = IMAGE_BASE_URL + SIZE + poster;
        }
    }
}
