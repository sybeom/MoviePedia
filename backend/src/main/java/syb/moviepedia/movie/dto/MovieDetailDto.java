package syb.moviepedia.movie.dto;

import lombok.Builder;

import java.util.List;

/**
 * 영화 상세 페이지에 출력될 정보 클래스. 프론트 응답 DTO
 * @param code 영화 ID
 * @param title 제목
 * @param backdropPath 상단 배너 (배경)
 * @param posterPath 포스터 경로
 * @param genres 장르
 * @param overview 개요
 * @param releaseYear 개봉연도
 * @param country 제작 국가 (공동 제작이 있을 수 있으므로 List 형태)
 * @param runtime 러닝 타임
 * @param globalRating 글로벌 평점
 */
@Builder
public record MovieDetailDto(
        Long code,
        String title,
        String backdropPath,
        String posterPath,
        List<String> genres,
        String overview,
        Integer releaseYear,
        List<String> country,
        Integer runtime,
        Double globalRating,
        List<MovieCreditDto> credit
) {
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/original";

    public MovieDetailDto {
        if (posterPath != null) {
            posterPath = IMAGE_BASE_URL + posterPath;
        }

        if (backdropPath != null) {
            backdropPath = IMAGE_BASE_URL + backdropPath;
        }
    }
}
