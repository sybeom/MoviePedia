package syb.moviepedia.tv.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syb.moviepedia.common.MediaCategoryType;
import syb.moviepedia.movie.repository.CountryRepository;
import syb.moviepedia.movie.repository.GenreRepository;
import syb.moviepedia.tv.domain.TV;
import syb.moviepedia.tv.domain.TVCategory;
import syb.moviepedia.tv.domain.TVSeries;
import syb.moviepedia.tv.external.TmdbTVClient;
import syb.moviepedia.tv.external.dto.TmdbContentRating;
import syb.moviepedia.tv.external.dto.TmdbTVDiscover;
import syb.moviepedia.tv.external.dto.TmdbTVSeries;
import syb.moviepedia.tv.repsitory.TVCategoryRepository;
import syb.moviepedia.tv.repsitory.TVRepository;
import syb.moviepedia.tv.repsitory.TVSeriesGenreRepository;
import syb.moviepedia.tv.repsitory.TVSeriesRepository;

import java.util.List;
import java.util.Objects;

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
    private static final String TITLE_PATTERN = "^(?!(?=.*\\p{L})(?!.*[가-힣]))[\\p{L}0-9 .,:~!?'\"/(){}\\[\\]&+\\-·]+$";

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
//        Set<Integer> seriesCodes = tmdbTVClient.fetchTVPopularCategories().results().stream()
//                .filter(result -> isTitleMatch(result.title()))
//                .map(result -> result.code())
//                .collect(Collectors.toSet());

//        List<TV> tvList = tvRepo.findByPopularSeason(seriesCodes);

//        List<TVCategory> categories = tvList.stream().map(tv -> TVCategory.builder()
//                        .code(tv.getCode())
//                        .title(tv.getTitle())
//                        .backdropPath("E")
//                        .mediaCategoryType(MediaCategoryType.POPULAR)
//                        .tv(tv)
//                        .build())
//                .toList();
        List<TVCategory> categories = tmdbTVClient.fetchTVPopularCategories().results().stream()
                .filter(result -> isTitleMatch(result.title()))
                .map(result -> TVCategory.builder()
                        .code(result.code())
                        .title(result.title())
                        .backdropPath(result.backdropPath())
                        .mediaCategoryType(MediaCategoryType.POPULAR)
                        .build())
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
