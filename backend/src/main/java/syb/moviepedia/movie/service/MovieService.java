package syb.moviepedia.movie.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syb.moviepedia.common.MovieCategoryType;
import syb.moviepedia.movie.domain.Cast;
import syb.moviepedia.movie.domain.Movie;
import syb.moviepedia.movie.domain.MovieCategory;
import syb.moviepedia.movie.dto.MovieCastDto;
import syb.moviepedia.movie.dto.MovieCategoriesDto;
import syb.moviepedia.movie.dto.MovieDetailDto;
import syb.moviepedia.movie.dto.MovieSummaryDto;
import syb.moviepedia.movie.external.tmdb.TmdbClient;
import syb.moviepedia.movie.external.tmdb.dto.TmdbGenre;
import syb.moviepedia.movie.external.tmdb.dto.TmdbMovieCertification;
import syb.moviepedia.movie.external.tmdb.dto.TmdbMovieDetail;
import syb.moviepedia.movie.repository.CountryRepository;
import syb.moviepedia.movie.repository.MovieCastRepository;
import syb.moviepedia.movie.repository.MovieCategoryRepository;
import syb.moviepedia.movie.repository.MovieRepository;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class MovieService {
    private final TmdbClient tmdbClient;
    private final MovieRepository movieRepository;
    private final MovieCategoryRepository movieCategoryRepository;
    private final CountryRepository countryRepository;
    private final MovieCastRepository movieCastRepository;

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

    /**
     * 영화 상세 정보 가져오기
     * 영화 ID 기준 DB에 없다면 영화 상세 API를 호출하여 추가 상세 정보(국가, 관람등급,런타임 등)을 추가하여 DB 저장.
     * DB에 있더라도 detailFetched가 false면 일부 상세 정보(국가, 관람등급, 런타임 등)이 비어있는 상태이므로 업데이트한다.
     */
    @Transactional
    public MovieDetailDto getMovieDetail(Long movieId) {
        // 영화 상세
        Movie movie = movieRepository.findByCode(movieId) // DB에 영화 존재하면 가져오고 아니면 상세 api 호출 후 영화 저장
                .orElseGet(() -> {
                    TmdbMovieDetail detail = tmdbClient.getMovieDetail(movieId);
                    log.info(detail.country().get(0));
                    String certification = extractCertification(tmdbClient.getMovieCertification(movieId));
                    return movieRepository.save(toMovieFromDetail(detail, certification));
                });

        if(!movie.getDetailFetched()) { // 영화가 있더라도 기타 세부 사항이 채워져있지 않으면
            log.info("영화 상세 업데이트");
            TmdbMovieDetail detail = tmdbClient.getMovieDetail(movieId);
            log.info(detail.country().get(0));
            String certification = extractCertification(tmdbClient.getMovieCertification(movieId));
            updateMovie(movie, detail, certification);
        }

        // 출연 - 없으면 api 호출후 db저장, 있으면 db에서 가져옴
        //
        List<MovieCastDto> castDto = toMovieCastDto(getCast(movie));
        return toMovieDetailDto(movie, castDto);
    }

    @Transactional
    public List<Cast> getCast(Movie movie) {
        Long movieId = movie.getId();
        List<Cast> cast = movieCastRepository.findByMovieIdOrderByCastOrderAsc(movieId);

        if (!cast.isEmpty()) { // DB에 있으면 반환
            log.info("getCast(): 캐스트 존재 그대로 반환");
            return cast;
        }
        log.info("getCast(): 영화 캐스트 api 호출");
        // 없으면 출연 정보 api 호출 후 MovieCast 저장
        cast =tmdbClient.getCredit(movieId).stream() // 출연 배우 목록 뽑기
                .map(tmdbCast -> Cast.builder()
                        .movie(movie)
                        .actorId(tmdbCast.actorId())
                        .name(tmdbCast.name())
                        .profile(tmdbCast.profile())
                        .castOrder(tmdbCast.castOrder())
                        .build())
                .toList();
        return movieCastRepository.saveAll(cast);
    }


    // 카테고리 영화 -> 영화 요약 DTO 가공
    private MovieSummaryDto toMovieSummaryDto(MovieCategory category) {
        // TODO: N+1 문제 추후 해결해보기
        Movie movie = category.getMovie(); // N+1 문제 발생할 수 있음. 추후 알아보고 수정

        return MovieSummaryDto.builder()
                .code(movie.getCode())
                .title(movie.getTitle())
                .poster(movie.getPosterPath())
                .certification(movie.getCertification())
                .genre(movie.getGenres())
                .build();
    }

    // 영화 상세 정보 -> 영화 엔티티 가공
    private Movie toMovieFromDetail(TmdbMovieDetail detail, String certification) {
        return Movie.builder()
                .code(detail.id())
                .title(detail.title())
                .posterPath(detail.posterPath())
                .backdropPath(detail.backdropPath())
                .genres(extractGenresFromDetail(detail.genres()))
                .certification(certification)
                .overview(detail.overview())
                .releaseDate(detail.releaseYear())
                .country(detail.country())
                .runtime(detail.runtime())
                .globalRating(detail.globalRating())
                .detailFetched(true)
                .build();
    }

    // 영화 상세 DTO 가공
    private MovieDetailDto toMovieDetailDto(Movie movie, List<MovieCastDto> dto) {
        return MovieDetailDto.builder()
                .code(movie.getCode())
                .title(movie.getTitle())
                .posterPath(movie.getPosterPath())
                .backdropPath(movie.getBackdropPath())
                .genres(movie.getGenres())
                .overview(movie.getOverview())
                .releaseYear(movie.getReleaseDate().getYear())
                .country(movie.getCountry())
                .runtime(movie.getRuntime())
                .globalRating(movie.getGlobalRating())
                .cast(dto)
                .build();
    }

    private List<MovieCastDto> toMovieCastDto(List<Cast> list) {
        return list.stream().map(cast -> MovieCastDto.builder()
                .actorId(cast.getActorId())
                .name(cast.getName())
                .profile(cast.getProfile())
                .order(cast.getCastOrder())
                .build())
                .toList();
    }

    // 영화 추가 정보(등급, 국가, 런타임 등) 업데이트
    private void updateMovie(Movie movie, TmdbMovieDetail detail, String  certification) {
        // 상세 api 호출 (국가정보, 런타임)
        List<String> countries = countryRepository.findNameByCodeIn(detail.country()); // 국가 코드 한국어 매핑
        Integer runtime = detail.runtime();

        movie.update(certification, countries, runtime);
    }

    // 상세 영화 정보 장르 추출
    private List<String> extractGenresFromDetail(List<TmdbGenre> genres) {
        return genres.stream().map(genre -> genre.name()).toList();
    }

    // 관람 등급 추출
    private String extractCertification(TmdbMovieCertification response) {
        return response.results().stream()
                .filter(releaseData -> releaseData.iso31661().equals("KR")) // 한국만 추출
                .filter(releaseData -> releaseData.releaseDates() != null)
                .flatMap(releaseDates -> releaseDates.releaseDates().stream()) // release_dates[] 평탄화
                .filter(release -> release.certification() != null && !release.certification().isBlank())
                .filter(release -> release.type() == 3) // type==3 : 극장 개봉
                .findFirst()
                .map(info -> info.certification())
                .orElse("등급 미정");
    }
}
