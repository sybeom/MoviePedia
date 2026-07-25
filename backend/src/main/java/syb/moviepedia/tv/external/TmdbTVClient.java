package syb.moviepedia.tv.external;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import syb.moviepedia.common.exception.TmdbApiException;
import syb.moviepedia.movie.external.tmdb.dto.TmdbVideoResponse;
import syb.moviepedia.tv.external.dto.*;

import java.net.URI;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class TmdbTVClient {

    private final WebClient tmdbWebClient;
    private final static String TV_DISCOVER_PATH = "/discover/tv";
    private final static String SERIES_PATH = "/tv/{seriesId}";
    private final static String TV_GENRES = "/genre/tv/list";
    private final static String TV_POPULAR = "/tv/popular";
    private final static String TV_CONTENT_RATING = "/tv/{series_code}/content_ratings";
    private final static String TV_SEASON_PATH = "/tv/{series_id}/season/{season_number}";
    private final static String TV_SEASON_CREDIT = "/tv/{series_id}/season/{season_number}/credits";
    private final static String TV_SEASON_VIDEO = "/tv/{series_id}/season/{season_number}/videos";

     // TV 시리즈 목록 api
    public TmdbTVDiscover fetchTVSeries(int page) {
        return get(
                TV_DISCOVER_PATH,
                TmdbTVDiscover.class,
                "Tmdb TV Discover api 호출 실패",
                uriBuilder -> uriBuilder
                        .queryParam("language", "ko-KR")
                        .queryParam("page", page)
                        .queryParam("sort_by", "popularity.desc")
        );
    }

    // 시리즈 상세 정보 api
    public TmdbTVSeries fetchTVSeriesDetail(Integer seriesCode) {
        return get(
                SERIES_PATH,
                TmdbTVSeries.class,
                "Tmdb TV Series Detail api 호출 실패",
                uriBuilder -> uriBuilder
                        .queryParam("language", "ko-KR"),
                seriesCode
        );
    }

    // 시즌 상세 정보
    public TmdbTVSeason fetchTVSeasonDetail(Integer seriesCode, Integer seasonNumber) {
        return get(
                TV_SEASON_PATH,
                TmdbTVSeason.class,
                "Tmdb TV 시즌 상세 api 호출 실패",
                uriBuilder -> uriBuilder
                        .queryParam("language", "ko-KR"),
                seriesCode,
                seasonNumber
        );
    }

    // 인기 TV 시리즈 api
    public TmdbTVCategory fetchTVPopularCategories() {
        return get(
                TV_POPULAR,
                TmdbTVCategory.class,
                "Tmdb TV Popular Category api 호출 실패",
                uriBuilder -> uriBuilder
                        .queryParam("page", 1)
                        .queryParam("language", "KO-KR")
        );
    }

    // TV 시리즈 관람 등급 api
    public TmdbContentRating fetchContentRating(Integer code) {
        return get(
                TV_CONTENT_RATING,
                TmdbContentRating.class,
                "Tmdb TV 관람 등급 api 호출 실패",
                null,
                code
        );
    }

    // TV 시리즈 시즌 크레딧
    public TmdbTVCredit fetchTVSeriesCredits(Integer seriesCode, Integer seasonNum) {
        return get(
                TV_SEASON_CREDIT,
                TmdbTVCredit.class,
                "Tmdb TV 시즌 크레딧 호출 실패",
                uriBuilder -> uriBuilder
                        .queryParam("language", "ko-KR"),
                seriesCode, seasonNum
        );
    }

    // TV 시리즈 시즌
    public TmdbVideoResponse fetchSeasonVideo(Integer seriesCode, Integer seasonNum, String language) {
        return get(
                TV_SEASON_VIDEO,
                TmdbVideoResponse.class,
                "Tmdb TV 시즌 비디오 호출 실패",
                uriBuilder -> uriBuilder
                        .queryParam("language", language),
                seriesCode, seasonNum
        );
    }

    private <T> T get(
            String path,
            Class<T> responseType,
            String errorMessage,
            Consumer<UriBuilder> queryParams,
            Object... uriVariables // movieId가 없는 경우도 있으므로 가변 인자로 설정
    ) {
        try {
            return tmdbWebClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path(path);

                        if (queryParams != null) {
                            queryParams.accept(uriBuilder);
                        }
                        URI uri = uriBuilder.build(uriVariables);
                        log.info("TMDB 요청 URI = " + uri);

                        return uriBuilder.build(uriVariables);
                    })
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();

        } catch (Exception e) {
            throw new TmdbApiException(errorMessage + ". 경로= " + path, e);
        }
    }
}
