package syb.moviepedia.movie.dto;

/**
 * 검색 결과 프론트 응답 DTO 클래스
 */
public record KeywordResponse(
        Long code,
        String title
) {
}
