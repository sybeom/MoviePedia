package syb.moviepedia.movie.dto;

import lombok.Builder;

import java.util.List;

/**
 * 홈 화면에 출력될 영화 카테고리별 영화 리스트 응답 DTO
 */
@Builder
public record MovieCategoriesDto(
        List<MovieSummaryDto> popular,
        List<MovieSummaryDto> upcoming,
        List<MovieSummaryDto> nowPlaying
) {

}
