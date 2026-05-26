package syb.moviepedia.movie.dto.response;

import lombok.Builder;

/**
 * 검색 결과 프론트 응답 DTO 클래스
 */
@Builder
public record KeywordResponse(
        Long code,
        String title
) {
}
