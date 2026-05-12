package syb.moviepedia.movie.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syb.moviepedia.common.MovieCategoryType;
import syb.moviepedia.common.ReleaseType;
import syb.moviepedia.movie.domain.Movie;
import syb.moviepedia.movie.domain.MovieCategory;
import syb.moviepedia.movie.dto.MovieCategoriesDto;
import syb.moviepedia.movie.dto.MovieDetailDto;
import syb.moviepedia.movie.dto.MovieSummaryDto;
import syb.moviepedia.movie.external.tmdb.TmdbClient;
import syb.moviepedia.movie.external.tmdb.dto.TmdbGenreList;
import syb.moviepedia.movie.external.tmdb.dto.TmdbMovieCertification;
import syb.moviepedia.movie.external.tmdb.dto.TmdbMovieReleaseInfo;
import syb.moviepedia.movie.repository.MovieCategoryRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class MovieService {
    private final TmdbClient tmdbClient;
    private final MovieCategoryRepository movieCategoryRepository;

    @Transactional(readOnly = true)
    public MovieCategoriesDto getCategoryMovies() {

        // 각 카테고리별 데이터 가져오기
        List<MovieCategory> popularList = movieCategoryRepository.findByCategoryTypeOrderByPopularityDesc(MovieCategoryType.POPULAR);
        List<MovieCategory> upcomingList = movieCategoryRepository.findByCategoryTypeOrderByPopularityDesc(MovieCategoryType.UPCOMING);
        List<MovieCategory> nowPlayingList = movieCategoryRepository.findByCategoryTypeOrderByPopularityDesc(MovieCategoryType.NOW_PLAYING);

        // DTO로 가공
        List<MovieSummaryDto> popularListDto = popularList.stream().map(response -> toMovieSummaryDto(response)).toList();
        List<MovieSummaryDto> upcomingListDto = upcomingList.stream().map(response -> toMovieSummaryDto(response)).toList();
        List<MovieSummaryDto> nowPlayingListDto = nowPlayingList.stream().map(response -> toMovieSummaryDto(response)).toList();

        log.info("Popular movies found: {}", popularList);
        return MovieCategoriesDto.builder()
                .popular(popularListDto)
                .upcoming(upcomingListDto)
                .nowPlaying(nowPlayingListDto)
                .build();
    }

    // 영화 상세 (영화 상세는 변환할 데이터가 크게 없기에 Dto 그대로 반환)
    public MovieDetailDto getMovieDetail(Long movieId) {
        return tmdbClient.getMovieDetail(movieId);
    }

    // 장르 리스트 Map 변환
    private Map<Integer,String> genreListToMap(TmdbGenreList genreList) {
        return genreList.genres().stream()
                .collect(Collectors.toMap(
                        genre -> genre.id(),
                        genre -> genre.name()
                ));
    }

    // 카테고리 영화 -> 영화 요약 DTO 가공
    private MovieSummaryDto toMovieSummaryDto(MovieCategory category) {

        // TODO: N+1 문제 추후 해결해보기
        Movie movie = category.getMovie(); // N+1 문제 발생할 수 있음. 추후 알아보고 수정

        return MovieSummaryDto.builder()
                .movieCode(movie.getMovieId())
                .title(movie.getTitle())
                .poster(movie.getPosterPath())
                .certification(movie.getCertification())
                .genre(movie.getGenres())
                .build();
    }

    // 관람 등급 추출
    private String extractCertification(TmdbMovieCertification certification) {
        List<TmdbMovieReleaseInfo> releaseInfoList = certification.results().stream()
                .filter(country -> "KR".equals(country.iso31661())) // 개봉 국가 한국인것만 꺼냄
                .filter(country -> country.releaseDates() != null)
                .flatMap(country ->
                        country.releaseDates().stream())
                            .filter(date -> date.certification() != null && !date.certification().isBlank())
                .toList();
        return releaseInfoList.stream()
                .filter(info ->
                        ReleaseType.THEATRICAL.matches(info.type()) || ReleaseType.DIGITAL.matches(info.type())) // 극장 개봉 및 OTT 개봉 둘 다 가져오기
                .map(info -> info.certification())
                .findFirst()
                .orElse("등급 미정");
    }
}
