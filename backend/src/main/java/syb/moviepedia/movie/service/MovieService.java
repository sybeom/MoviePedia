package syb.moviepedia.movie.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syb.moviepedia.comment.repository.CommentRepository;
import syb.moviepedia.common.CreditRole;
import syb.moviepedia.common.MovieCategoryType;
import syb.moviepedia.movie.domain.Credit;
import syb.moviepedia.movie.domain.Movie;
import syb.moviepedia.movie.domain.MovieCategory;
import syb.moviepedia.movie.dto.response.*;
import syb.moviepedia.movie.external.tmdb.TmdbClient;
import syb.moviepedia.movie.external.tmdb.dto.*;
import syb.moviepedia.movie.repository.CountryRepository;
import syb.moviepedia.movie.repository.MovieCategoryRepository;
import syb.moviepedia.movie.repository.MovieCreditRepository;
import syb.moviepedia.movie.repository.MovieRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class MovieService {
    private final TmdbClient tmdbClient;
    private final MovieRepository movieRepository;
    private final MovieCategoryRepository movieCategoryRepository;
    private final CountryRepository countryRepository;
    private final MovieCreditRepository movieCreditRepository;
    private final CommentRepository commentRepository;

    @Transactional(readOnly = true)
    public MovieCategoriesResponse getCategoryMovies() {
        log.info("영화 엔티티 수: {}", movieRepository.count());
        List<MovieCategory> popularList = movieCategoryRepository.findByCategoryTypeOrderByPopularityDesc(MovieCategoryType.POPULAR);
        List<MovieCategory> upcomingList = movieCategoryRepository.findByCategoryTypeOrderByPopularityDesc(MovieCategoryType.UPCOMING);
        List<MovieCategory> nowPlayingList = movieCategoryRepository.findByCategoryTypeOrderByPopularityDesc(MovieCategoryType.NOW_PLAYING);

        // DTO로 가공
        List<MovieSummaryResponse> popularListDto = popularList.stream().map(response -> toMovieSummaryDto(response)).toList();
        List<MovieSummaryResponse> upcomingListDto = upcomingList.stream().map(response -> toMovieSummaryDto(response)).toList();
        List<MovieSummaryResponse> nowPlayingListDto = nowPlayingList.stream().map(response -> toMovieSummaryDto(response)).toList();

        log.info("Popular movies found: {}", popularList);
        return MovieCategoriesResponse.builder()
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
    public MovieDetailResponse getMovieDetail(Long mvCode) {
        // 영화 상세
        Movie movie = movieRepository.findByCode(mvCode) // DB에 영화 존재하면 가져오고 아니면 상세 api 호출 후 영화 저장
                .orElseGet(() -> {
                    log.info("DB 영화 존재 X, DB 저장 시작");
                    TmdbMovieDetail detail = tmdbClient.getMovieDetail(mvCode);
                    String certification = extractCertification(tmdbClient.getMovieCertification(mvCode));
                    return movieRepository.save(toMovieFromDetail(detail, certification));
                });

        // 영화가 있더라도 기타(등급, 런타임, 국가) 채워져있지 않을 때.
        // 보통 카테고리 영화 저장시 기타 정보는 저장되지 않아 상세 페이지 조회시 실행됨
        if(!movie.getDetailFetched()) {
            log.info("getMovieDetail(): 영화 상세 업데이트");
            TmdbMovieDetail detail = tmdbClient.getMovieDetail(mvCode);

//            // TODO: 확인 결과 등급은 초기 데이터 설정시 카테고리에서 채우는 것같은데 중복 저장아닌지 . 확인해보기
//            String certification = extractCertification(tmdbClient.getMovieCertification(mvCode));
            updateMovie(movie, detail);
        }

        // 크레딧(출연) - 없으면 api 호출후 db저장, 있으면 db에서 가져옴
        List<MovieCreditResponse> creditDto = toMovieCreditDto(getCredit(movie));

        // 영화의 코멘트가 20개 이상이면 평점 조회
        Double rating = movie.getDisplayRating();

        return toMovieDetailResponse(movie, creditDto, rating);
    }

    // 출연 배우
    @Transactional
    public List<Credit> getCredit(Movie movie) {
        Long movieId = movie.getId();
        List<Credit> credits = movieCreditRepository.findByMovieId(movieId);

        if (!credits.isEmpty()) {
            log.info("getCast(): 크레딧 정보 존재. 그대로 반환");
            return credits;
        }

        // 없으면 크레딧 api 호출 후 MovieCredit 저장, 가져옴
        TmdbCredit tmdbCredit = tmdbClient.getCredit(movie.getCode());
        List<TmdbCrew> crews = tmdbCredit.crew();
        List<TmdbCast> casts = tmdbCredit.cast();

        credits = new ArrayList<>();

        // 감독 정보 Credit에 넣기
        credits.addAll(crews.stream()
                .filter(crew -> crew.job().equals(CreditRole.DIRECTOR.getRole()))
                .map(crew -> Credit.builder()
                        .role(CreditRole.DIRECTOR)
                        .movie(movie)
                        .name(crew.name())
                        .profile(crew.profile())
                        .castOrder(null).build())
                .toList());

        // 출연 배우 정보 Credit에 넣기
        credits.addAll(casts.stream()
                .map(cast -> Credit.builder()
                        .role(CreditRole.ACTOR)
                        .movie(movie)
                        .name(cast.name())
                        .profile(cast.profile())
                        .castOrder(cast.castOrder())
                        .build())
                .limit(10) // 출연 배우는 10명만
                .toList());

        log.info("getCast(): 영화 캐스트 api 호출");
        // 없으면 출연 정보 api 호출 후 MovieCast 저장
        return movieCreditRepository.saveAll(credits);
    }


    // 카테고리 영화 -> 영화 요약 DTO 가공
    private MovieSummaryResponse toMovieSummaryDto(MovieCategory mc) {
        Movie movie = mc.getMovie();

        return MovieSummaryResponse.builder()
                .code(movie.getCode())
                .title(movie.getTitle())
                .poster(movie.getPosterPath())
                .certification(movie.getCertification())
                .genre(movie.getGenres())
                .build();
    }

    // 영화 상세 정보 -> 영화 엔티티 가공
    private Movie toMovieFromDetail(TmdbMovieDetail detail, String certification) {
        List<String> countries = countryRepository.findNameByCodeIn(detail.country());
        return Movie.builder()
                .code(detail.id())
                .title(detail.title())
                .posterPath(detail.posterPath())
                .backdropPath(detail.backdropPath())
                .genres(extractGenresFromDetail(detail.genres()))
                .certification(certification)
                .overview(detail.overview())
                .releaseDate(detail.releaseYear())
                .country(countries)
                .runtime(detail.runtime())
                .globalRating(detail.globalRating())
                .detailFetched(true)
                .build();
    }

    // 영화 상세 DTO 가공
    private MovieDetailResponse toMovieDetailResponse(Movie movie, List<MovieCreditResponse> dto, Double rating) {
        return MovieDetailResponse.builder()
                .code(movie.getCode())
                .title(movie.getTitle())
                .posterPath(movie.getPosterPath())
                .backdropPath(movie.getBackdropPath())
                .genres(movie.getGenres())
                .certification(movie.getCertification())
                .overview(movie.getOverview())
                .releaseYear(movie.getReleaseDate().getYear())
                .country(movie.getCountry())
                .runtime(movie.getRuntime())
                .rating(rating)
                .globalRating(movie.getGlobalRating())
                .credit(dto)
                .build();
    }

    // Credit 엔티티 영화 크레딧 DTO로 가공
    private List<MovieCreditResponse> toMovieCreditDto(List<Credit> list) {
        return list.stream().map(credit -> MovieCreditResponse.builder()
                        .role(credit.getRole())
                        .name(credit.getName())
                        .profile(credit.getProfile())
                        .build())
                .toList();
    }

    // 영화 추가 정보(국가, 런타임 등) 업데이트
    private void updateMovie(Movie movie, TmdbMovieDetail detail) {
        // 상세 api 호출 (국가정보, 런타임)
        List<String> countries = countryRepository.findNameByCodeIn(detail.country()); // 국가 코드 한국어 매핑
        Integer runtime = detail.runtime();

        movie.update(countries, runtime);
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

    // 영화 검색 목록
    public List<KeywordResponse> getKeywords(String keyword) {
        return tmdbClient.getKeywordList(keyword).results().stream().map(
                tmdbKeyword -> KeywordResponse.builder()
                        .code(tmdbKeyword.id())
                        .title(tmdbKeyword.title())
                        .build())
                .toList();
    }
}
