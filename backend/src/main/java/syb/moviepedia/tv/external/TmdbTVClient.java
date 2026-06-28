package syb.moviepedia.tv.external;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import syb.moviepedia.common.exception.TmdbApiException;
import syb.moviepedia.tv.external.dto.TmdbTVCategory;
import syb.moviepedia.tv.external.dto.TmdbTVDiscover;
import syb.moviepedia.tv.external.dto.TmdbTVSeries;

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


     // TV 시리즈 목록 api
    public TmdbTVDiscover fetchTVSeries(int page) {
        return get(
                TV_DISCOVER_PATH,
                TmdbTVDiscover.class,
                "Tmdb TV Discover api 호출 실패",
                uriBuilder -> uriBuilder
                        .queryParam("page", page)
                        .queryParam("language", "KO-KR")
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
