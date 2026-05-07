package syb.moviepedia.movie.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import syb.moviepedia.common.ReleaseType;
import syb.moviepedia.member.repository.MemberRepository;
import syb.moviepedia.movie.dto.MovieDetailDto;
import syb.moviepedia.movie.dto.TmdbMovieSummaryDto;
import syb.moviepedia.movie.external.tmdb.TmdbClient;
import syb.moviepedia.movie.external.tmdb.dto.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class MovieService {
    private final TmdbClient tmdbClient;
    private MemberRepository memberRepository;
    /**
     * 인기 영화 흐름 *
     * 인기 영화 목록 APi 호출 및 목록(json) 가져옴 -> 장르 API 호출 및 장르 정보 가져옴
     * -> 얻은 장르 정보 Map에 담기 -> 인기 영화목록의 장르 값과 장르 정보를 맵핑
     * -> 동시에 응답 객체로 변환
     */
    // 인기 영화
    public List<TmdbMovieSummaryDto> getPopularMovies() {
        TmdbMovieSummaryList movieResponse = tmdbClient.getPopularMovies();
        TmdbGenreList genreResponse = tmdbClient.getMovieGenres();

        // 장르 정보 Map 저장
        Map<Integer, String> genreMap = genreListToMap(genreResponse);

        return movieResponse.results().stream()
                .map(movie ->
                        toMovieSummaryDto(movie, genreMap, tmdbClient.getMovieCertification(movie.id()), true))
                .toList();
    }

    // 상영중인 영화
    public List<TmdbMovieSummaryDto> getNowPlayingMovies() {
        TmdbMovieSummaryList movieResponse = tmdbClient.getNowPlayingMovies();
        TmdbGenreList genreResponse = tmdbClient.getMovieGenres();

        // 장르 정보 Map 저장
        Map<Integer, String> genreMap = genreListToMap(genreResponse);

        return movieResponse.results().stream()
                .map(movie ->
                        toMovieSummaryDto(movie, genreMap, tmdbClient.getMovieCertification(movie.id()), true))
                .toList();
    }

    // 개봉 예정작
    public List<TmdbMovieSummaryDto> getUpcomingMovies() {
        TmdbMovieSummaryList movieResponse = tmdbClient.getUpcomingMovies();
        TmdbGenreList genreResponse = tmdbClient.getMovieGenres();

        // 장르 정보 Map 저장
        Map<Integer, String> genreMap = genreListToMap(genreResponse);

        return movieResponse.results().stream()
                .map(movie ->
                        toMovieSummaryDto(movie, genreMap, tmdbClient.getMovieCertification(movie.id()), false))
                .toList();
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

    // 프론트 응답 Json -> 영화 요약 dto 변환
    private TmdbMovieSummaryDto toMovieSummaryDto(
            TmdbMovieSummary movie,
            Map<Integer, String> genreMap,
            TmdbMovieCertification movieCertification,
            boolean includeVoteAverage
    ) {
        // 장르 번호에 해당하는 장르를 맵에서 가져옴(장르는 여럿 있을 수 있다)
        List<String> genreNames = extractGenres(movie, genreMap);

        // 관람 등급 추출
        String certification= extractCertification(movieCertification);

        return TmdbMovieSummaryDto.builder()
                .id(movie.id())
                .title(movie.title())
                .poster(generatePosterURL(movie.posterPath()))
                .genre(genreNames)
                .certification(certification)
                .voteAverage(includeVoteAverage ? movie.voteAverage() : null) // 개봉예정에는 평점없도록하기 위해 voteAverage은 null
                .build();
    }

    // 장르 번호에 해당하는 장르들 추출
    private List<String> extractGenres(TmdbMovieSummary movie, Map<Integer, String> genreMap) {
        return movie.genreIds().stream()
                .map(genreId -> genreMap.get(genreId))
                .filter(Objects::nonNull)
                .toList();
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

    // 포스터 완전 URL로 변경(포스터는 파일 경로만 데이터로 오기때문에 완전한 URL로 변경한다)
    private String generatePosterURL(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }
        String size = "original";
        return  "https://image.tmdb.org/t/p/" + size + path; // TODO: 이거 경로 상수화 하기
    }
}
