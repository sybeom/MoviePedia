package syb.moviepedia.movie.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import syb.moviepedia.common.CountryCode;
import syb.moviepedia.movie.external.tmdb.dto.TmdbGenre;

import java.util.List;

/**
 * 영화 상세 페이지에 출력될 정보 클래스
 * @param id 영화 ID
 * @param title 제목
 * @param backdropPath 상단 배너 (배경)
 * @param posterPath 포스터 경로
 * @param genres 장르
 * @param overview 개요
 * @param releaseDate 개봉일
 * @param country 제작 국가 (공동 제작이 있을 수 있으므로 List 형태)
 * @param runtime 러닝 타임
 * @param globalRating 글로벌 평점
 */
public record MovieDetailDto(
        Long id,

        String title,

        @JsonProperty("backdrop_path")
        String backdropPath,

        @JsonProperty("poster_path")
        String posterPath,

        List<TmdbGenre> genres,

        String overview,

        @JsonProperty("release_date")
        String releaseDate,

        @JsonProperty("origin_country")
        List<String> country,

        String runtime,

        @JsonProperty("vote_average")
        String globalRating
) {
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/original";

    public MovieDetailDto { // record는 this를 사용하지 않는다.
        // 포스터 경로 완전 경로로 변경
        if (posterPath != null) {
            posterPath = IMAGE_BASE_URL + posterPath;
        }

        // 백드롭 경로 완전 경로로 변경
        if (backdropPath != null) {
            backdropPath = IMAGE_BASE_URL + backdropPath;
        }

        // 국가명 한국어로 변환
        country = country == null
                ? List.of()
                : country.stream()
                .map(CountryCode::toKoreanName)
                .toList();
    }
}
