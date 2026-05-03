package syb.moviepedia.movie.dto;

import lombok.Builder;

import java.util.List;

/**
 * 메인 화면에 표시될 영화 요약 응답 DTO
 */
@Builder
public record TmdbMovieSummaryDto(
        String title, // 제목
        String poster, // 포스터
        List<String> genre, // 장르
        String voteAverage // 평점
) {}