package syb.moviepedia.movie.external.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 개봉 날짜 api 응답 데이터를 매핑할 클래스
 * 개봉 날짜 api를 통해 영화 id에 해당하는 관람 등급을 얻을 수 있다.
 * 관람 등급을 얻기 위함이므로 클래스명을 개봉 날짜 관련보다는 관람 등급 관련으로 명명함.
 */
public record TmdbMovieCertification(
        Long id,
        List<TmdbMovieCertificationCountry> results) {

    /**
     * 개봉 날짜 api results 배열의 원소(국가 단위)를 나타내는 클래스
     * 개봉 날짜 api를 통해 영화 id에 해당하는 관람 등급을 얻을 수 있다.
     * 관람 등급을 얻기 위함이므로 클래스명을 개봉 날짜 관련보다는 관람 등급 관련으로 명명함.
     */
    public record TmdbMovieCertificationCountry(
            @JsonProperty(value = "iso_3166_1")
            String iso31661,
            @JsonProperty(value = "release_dates")
            List<TmdbMovieReleaseInfo> releaseDates
    ) {
        /**
         * 개봉 날짜 api results 배열의 원소의 release_dates 배열의 원소를 나타내는 클래스
         * 개봉 날짜 api를 통해 영화 id에 해당하는 관람 등급을 얻을 수 있다.
         * 관람 등급을 얻기 위함이므로 클래스명을 개봉 날짜 관련보다는 관람 등급 관련으로 명명함.
         */
        public record TmdbMovieReleaseInfo(
                String certification,
                Integer type
        ) {}
    }
}
/**
 * {                <<  TmdbMovieCertification.class
 *   "id": 640146,
 *   "results": [
 *     {                                << TmdbMovieCertificationCountry.class
 *       "iso_3166_1": "KR",
 *       "release_dates": [
 *         {                                        << TmdbMovieReleaseInfo.class
 *           "certification": "12",
 *           "descriptors": [],
 *           "iso_639_1": "",
 *           "note": "",
 *           "release_date": "2023-02-15T00:00:00.000Z",
 *           "type": 3
 *         },
 *         {
 *           "certification": "12",
 *           "descriptors": [],
 *           "iso_639_1": "",
 *           "note": "Disney+",
 *           "release_date": "2023-05-17T00:00:00.000Z",
 *           "type": 4
 *         }
 *       ]
 *     },
 *     {
 *      "iso_3166_1": "US",
 *      ...
 *     },
 *     {
 *      "iso_3166_1": "JP",
 *      ...
 *     },
 *   ]
 * }
 */
