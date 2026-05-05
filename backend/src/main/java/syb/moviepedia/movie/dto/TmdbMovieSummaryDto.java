package syb.moviepedia.movie.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.List;

/**
 * 메인 화면에 표시될 영화 요약 응답 DTO
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // 개봉 예정작 평점 제외하기 위함
@Builder
public record TmdbMovieSummaryDto(
        String title, // 제목
        String poster, // 포스터
        List<String> genre, // 장르
        String certification,
        String voteAverage // 평점
) {}