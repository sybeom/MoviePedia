package syb.moviepedia.movie.external.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

/**
 * 영화 상세 api 데이터 매핑 클래스
 * @param id 영화 ID
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
public record TmdbMovieDetail(
        Long id,
        String title,
        @JsonProperty("backdrop_path")
        String backdropPath,
        @JsonProperty("poster_path")
        String posterPath,
        List<TmdbGenre> genres,
        String overview,
        @JsonProperty("release_date")
        LocalDate releaseYear,
        @JsonProperty("origin_country")
        List<String> country,
        Integer runtime,
        @JsonProperty("vote_average")
        Double globalRating
) {
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/original";
    public TmdbMovieDetail {
        if (posterPath != null) {
            posterPath = IMAGE_BASE_URL + posterPath;
        }

        if (backdropPath != null) {
            backdropPath = IMAGE_BASE_URL + backdropPath;
        }

        // 글로벌 평점 소수점 첫째자리까지 표기
        // 0점이면 null (아직 평점 책정이 되지 않았다는 뜻이다)
        globalRating = globalRating==0 ? null : Math.round(globalRating * 10) / 10.0; // 소수점 둘째자리에서 반올림
    }
}
