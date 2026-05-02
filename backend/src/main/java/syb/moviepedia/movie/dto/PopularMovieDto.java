package syb.moviepedia.movie.dto;

import lombok.Builder;

import java.util.List;

/**
 * TMDB api 인기영화 목록 응답 DTO
 */
@Builder
public record PopularMovieDto(
        String title, // 제목
        String poster, // 포스터
        List<String> genre, // 장르
        String voteAverage // 평점
) {}