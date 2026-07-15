package syb.moviepedia.tv.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syb.moviepedia.common.MediaCategoryType;
import syb.moviepedia.movie.repository.CountryRepository;
import syb.moviepedia.movie.repository.GenreRepository;
import syb.moviepedia.tv.domain.TV;
import syb.moviepedia.tv.domain.TVCategory;
import syb.moviepedia.tv.domain.TVSeasonDocument;
import syb.moviepedia.tv.domain.TVSeries;
import syb.moviepedia.tv.external.TmdbTVClient;
import syb.moviepedia.tv.external.dto.TmdbContentRating;
import syb.moviepedia.tv.external.dto.TmdbTVCategory;
import syb.moviepedia.tv.external.dto.TmdbTVDiscover;
import syb.moviepedia.tv.external.dto.TmdbTVSeries;
import syb.moviepedia.tv.repsitory.TVCategoryRepository;
import syb.moviepedia.tv.repsitory.TVRepository;
import syb.moviepedia.tv.repsitory.TVSeriesGenreRepository;
import syb.moviepedia.tv.repsitory.TVSeriesRepository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TVInitService {
    private final TVRepository tvRepo;
    private final TVCategoryRepository tvCategoryRepo;
    private final TVSeriesRepository tvSeriesRepo;
    private final TVSeriesGenreRepository tvSeriesGenreRepo;
    private final TmdbTVClient tmdbTVClient;
    private final CountryRepository countryRepo;
    private final GenreRepository genreRepo;
    private final ElasticsearchOperations esOperations;
    private static final String TITLE_PATTERN = "^(?!(?=.*\\p{L})(?!.*[가-힣]))[\\p{L}0-9 .,:~!?'\"/(){}\\[\\]&+\\-·]+$";
    private static final String INDEX_NAME = "tv_season_search";

    // 엘라스틱서치에 저장
    @Transactional
    public void saveElasticTV() {

        Long lastId = 0L;
        int totalIndexedCount = 0;

        while (true) {
            // 1000개씩 조회
            List<TV> tvs = tvRepo.findTop1000ByIdGreaterThanOrderByIdAsc(lastId);

            if (tvs.isEmpty()) {
                break;
            }

            List<IndexQuery> queries = tvs.stream()
                    .map(tv -> {
                        TVSeasonDocument doc = TVSeasonDocument.from(tv);

                        // 문서 하나를 저장하기 위한 IndexQuery 객체를 만들어주는 빌더
                        // 문서를 ES에 저장하기 위한 요청 객체 (즉 저장 요청 정보)
                        return new IndexQueryBuilder()
                                .withId(doc.getId())
                                .withObject(doc)
                                .build();
                    })
                    .toList();

            esOperations.bulkIndex(queries, IndexCoordinates.of(INDEX_NAME));

            totalIndexedCount += tvs.size();

            lastId = tvs.get(tvs.size() - 1).getId();

            log.info("Elasticsearch 영화 색인 진행 중 - lastId={}, indexedCount={}",
                    lastId,
                    totalIndexedCount
            );
        }
    }

    public void initSeries() {
        List<TVSeries> seriesList = tvSeriesRepo.findAll();

        seriesList.forEach(series -> {
            TmdbTVSeries response = tmdbTVClient.fetchTVSeriesDetail(series.getCode());

            series.updateOverviewAndPosterPath(
                    response.overview(),
                    response.posterPath(),
                    response.backdropPath()
            );
        });
    }

    public void initCategories() {
        tvCategoryRepo.deleteAll();
        List<TmdbTVCategory.TmdbTVCategoryResult> results = tmdbTVClient.fetchTVPopularCategories().results().stream()
                .filter(result -> isTitleMatch(result.title()))
                .filter(result -> result.code() != 312949)
                .toList();

        Set<Integer> seriesCodes = results.stream()
                .map(TmdbTVCategory.TmdbTVCategoryResult::code)
                .collect(Collectors.toSet());

        List<TV> tvList = tvRepo.findByPopularSeason(seriesCodes);

        // TV의 code를 기준으로 빠르게 찾기 위한 Map
        Map<Integer, Integer> seasonNumberByCode = tvList.stream()
                .collect(Collectors.toMap(
                        TV::getSeriesCode,
                        TV::getSeasonNum
                ));

        List<TVCategory> categories = results.stream()
                .map(result -> {
                    Integer seasonNumber =
                            seasonNumberByCode.get(result.code());

                    if (seasonNumber == null) {
                        throw new IllegalStateException(
                                "TV를 찾을 수 없습니다. code: " + result.code()
                        );
                    }

                    return TVCategory.builder()
                            .seriesCode(result.code())
                            .seasonNumber(seasonNumber)
                            .title(result.title())
                            .backdropPath(result.backdropPath())
                            .mediaCategoryType(MediaCategoryType.POPULAR)
                            .build();
                })
                .toList();

        tvCategoryRepo.saveAll(categories);
    }

    public void initTV(int page) {
        // 시리즈 id를 반환
        TmdbTVDiscover tvSeries = tmdbTVClient.fetchTVSeries(page);

        List<TV> tvs = tvSeries.results().stream()
                .map(result -> tmdbTVClient.fetchTVSeriesDetail(result.seriesCode()))
                .filter(series -> isTitleMatch(series.title()))
                .flatMap(series -> series.seasons().stream()
                        .filter(season -> season.seasonNumber() != 0)
                        .map(season -> TV.builder()
                                .seriesCode(series.code())
                                .seasonNum(season.seasonNumber())
                                .episodeCnt(season.episodeCnt())
                                .build()
                        )
                )
                .toList();

        tvRepo.saveAll(tvs);

    }

    private String getCertification(TmdbContentRating tmdbContentRating) {
        return tmdbContentRating.results().stream()
                .filter(info -> "KR".equals(info.countryCode()))
                .map(info -> info.rating())
                .findFirst()
                .orElseGet(() -> tmdbContentRating.results().stream()
                        .filter(info -> "JP".equals(info.countryCode()))
                        .map(info -> mapUsTvRating(info.rating()))
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null)
                );
    }

    private String mapUsTvRating(String rating) {
        if (rating == null || rating.isBlank()) {
            return null;
        }

        return switch (rating) {
            case "TV-Y", "TV-Y7", "TV-G" -> "ALL";
            case "TV-PG" -> "12";
            case "TV-14" -> "15";
            case "TV-MA" -> "19";
            case "NR" -> null;
            default -> null;
        };
    }

    private boolean isTitleMatch(String title) {
        return title.matches(TITLE_PATTERN);
    }
}
