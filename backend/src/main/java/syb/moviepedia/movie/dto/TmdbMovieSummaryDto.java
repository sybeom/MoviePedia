package syb.moviepedia.movie.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.List;

/**
 * 메인 화면에 표시될 영화 요약 응답 DTO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record TmdbMovieSummaryDto(
        String title, // 제목
        String poster, // 포스터
        List<String> genre, // 장르
        String voteAverage // 평점
) {}