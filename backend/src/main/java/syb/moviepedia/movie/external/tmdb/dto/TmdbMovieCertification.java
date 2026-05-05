package syb.moviepedia.movie.external.tmdb.dto;

import java.util.List;

/**
 * 개봉 날짜 api 응답 데이터를 매핑할 클래스
 * 개봉 날짜 api를 통해 영화 id에 해당하는 관람 등급을 얻을 수 있다.
 * 관람 등급을 얻기 위함이므로 클래스명을 개봉 날짜 관련보다는 관람 등급 관련으로 명명함.
 */
public record TmdbMovieCertification(
        Long id,
        List<TmdbMovieCertificationCountry> results) {}
/**
 * {                <<  TmdbMovieCertification.class
 *   "id": 640146,
 *   "results": [
 *     {                                << TmdbMovieCertificationList.class
 *       "iso_3166_1": "KR",
 *       "release_dates": [
 *         {                                        << TmdbMovieReleaseDates.class
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
