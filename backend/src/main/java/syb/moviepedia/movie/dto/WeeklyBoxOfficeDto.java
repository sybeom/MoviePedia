package syb.moviepedia.movie.dto;

import lombok.Builder;

/**
 * 주간 박스오피스 응답 DTO
 */
@Builder
public record WeeklyBoxOfficeDto(
        String title,
        String poster,
        String genre,
        String nation
) { }
