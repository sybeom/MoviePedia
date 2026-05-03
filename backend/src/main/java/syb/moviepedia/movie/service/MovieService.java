package syb.moviepedia.movie.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import syb.moviepedia.movie.dto.TmdbMovieSummaryDto;
import syb.moviepedia.movie.external.tmdb.TmdbClient;
import syb.moviepedia.movie.external.tmdb.dto.TmdbGenreList;
import syb.moviepedia.movie.external.tmdb.dto.TmdbMovieSummary;
import syb.moviepedia.movie.external.tmdb.dto.TmdbMovieSummaryList;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

// TODO: 메서드 공통화 하기, Popular 클래스 통일하기, 개봉예정작은 평점이 나타나지 않아야한다.
// TODO: swagger 작성하기
@Slf4j
@RequiredArgsConstructor
@Service
public class MovieService {
    private final TmdbClient tmdbClient;
    /**
     * 인기 영화 흐름 *
     * 인기 영화 목록 APi 호출 및 목록 가져옴 -> 장르 API 호출 및 장르 정보 가져옴
     * -> 얻은 장르 정보 Map에 담기 -> 인기 영화목록의 장르 값과 장르 정보를 맵핑
     * -> 동시에 응답 객체로 변환
     */
    // 인기 영화
    public List<TmdbMovieSummaryDto> getPopularMovies() {
        TmdbMovieSummaryList movieResponse = tmdbClient.getPopularMovies();
        TmdbGenreList genreResponse = tmdbClient.getMovieGenres();

        // 장르 정보 Map 저장
        Map<Integer, String> genreMap = genreResponse.genres().stream()
                .collect(Collectors.toMap(
                        genre -> genre.id(),
                        genre -> genre.name()
                ));

        return movieResponse.results().stream()
                .map(movie -> toPopularMovieDto(movie, genreMap))
                .toList();
    }

    // 상영중인 영화
    public List<TmdbMovieSummaryDto> getNowPlayingMovies() {
        TmdbMovieSummaryList movieResponse = tmdbClient.getNowPlayingMovies();
        TmdbGenreList genreResponse = tmdbClient.getMovieGenres();

        // 장르 정보 Map 저장
        Map<Integer, String> genreMap = genreResponse.genres().stream()
                .collect(Collectors.toMap(
                        genre -> genre.id(),
                        genre -> genre.name()
                ));

        return movieResponse.results().stream()
                .map(movie -> toPopularMovieDto(movie, genreMap))
                .toList();
    }

    // 개봉 예정작
    public List<TmdbMovieSummaryDto> getUpcomingMovies() {
        TmdbMovieSummaryList movieResponse = tmdbClient.getUpcomingMovies();
        TmdbGenreList genreResponse = tmdbClient.getMovieGenres();

        // 장르 정보 Map 저장
        Map<Integer, String> genreMap = genreResponse.genres().stream()
                .collect(Collectors.toMap(
                        genre -> genre.id(),
                        genre -> genre.name()
                ));

        return movieResponse.results().stream()
                .map(movie -> toPopularMovieDto(movie, genreMap))
                .toList();
    }

    // 프론트 응답 DTO 변환
    private TmdbMovieSummaryDto toPopularMovieDto(
            TmdbMovieSummary movie,
            Map<Integer, String> genreMap
    ) {
        List<String> genreNames = movie.genreIds().stream()
                .map(genreId -> genreMap.get(genreId))
                .filter(Objects::nonNull)
                .toList();

        return TmdbMovieSummaryDto.builder()
                .title(movie.title())
                .poster(generatePosterURL(movie.posterPath()))
                .genre(genreNames)
                .voteAverage(movie.voteAverage())
                .build();
    }

    // 포스터 완전 URL로 변경(포스터는 파일 경로만 데이터로 오기때문에 완전한 URL로 변경한다)
    private String generatePosterURL(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }
        String size = "original";
        return  "https://image.tmdb.org/t/p/" + size + path;
    }
}
