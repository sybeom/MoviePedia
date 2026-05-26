package syb.moviepedia.movie.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syb.moviepedia.comment.domain.Comment;
import syb.moviepedia.comment.repository.CommentRepository;
import syb.moviepedia.common.MovieCategoryType;
import syb.moviepedia.common.ProviderType;
import syb.moviepedia.common.RoleType;
import syb.moviepedia.member.domain.Member;
import syb.moviepedia.member.repository.MemberRepository;
import syb.moviepedia.movie.domain.Country;
import syb.moviepedia.movie.domain.Genre;
import syb.moviepedia.movie.domain.Movie;
import syb.moviepedia.movie.domain.MovieCategory;
import syb.moviepedia.movie.external.tmdb.TmdbClient;
import syb.moviepedia.movie.external.tmdb.dto.TmdbMovie;
import syb.moviepedia.movie.external.tmdb.dto.TmdbMovieList;
import syb.moviepedia.movie.repository.CountryRepository;
import syb.moviepedia.movie.repository.GenreRepository;
import syb.moviepedia.movie.repository.MovieCategoryRepository;
import syb.moviepedia.movie.repository.MovieRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 영화 초기 데이터 설정을 위한 클래스
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MovieInitService {

    private final TmdbClient tmdbClient;
    private final MovieRepository movieRepository;
    private final MovieCategoryRepository movieCategoryRepository;
    private final GenreRepository tmdbGenreRepository;
    private final CountryRepository countryRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final CommentRepository commentRepository;

    // 장르 데이터 초기화(로드)
    public void initGenres() {
        // tmdb 장르 데이터는 총 19개
        if (tmdbGenreRepository.count() > 0) {
            log.info("initGenres(): 장르 데이터 DB존재");
            return;
        }

        log.info("initGenres() 실행, 장르 데이터 변경 감지");
        List<Genre> genres = tmdbClient.getMovieGenres().genres().stream()
                .filter(genre ->
                        !tmdbGenreRepository.existsByGenreId(genre.id())) // 존재하지 않는 값들만
                .map(genre ->
                        Genre.builder()
                                .genreId(genre.id())
                                .name(genre.name())
                                .build())
                .toList();
        tmdbGenreRepository.saveAll(genres);
    }

    // 국가 데이터 초기화
    public void initCountries() {
        if (countryRepository.count() > 0) {
            log.info("initCountries(): 국가 데이터 DB존재");
            return;
        }

        log.info("initCountries() 실행, 국가 데이터 변경 감지");
        List<Country> countries = tmdbClient.getCountries().stream()
                .map(country ->
                        Country.builder()
                                .code(country.code())
                                .name(country.name())
                                .build())
                .toList();
        countryRepository.saveAll(countries);
    }

    public void initMovies() {
        if (movieRepository.count() > 0) {
            return;
        }
        log.info("initMovies() 일반 영화 초기 데이터 호출");
        saveMovies(tmdbClient.getInitMovies());
    }

    private void saveMovies(TmdbMovieList responses) {
        List<Movie> movies = responses.results().stream()
                .filter(tmdbMv -> !movieRepository.existsByCode(tmdbMv.code())) // DB에 없는 영화들만
                .map(response ->{
                            String certification = extractCertification(response);
                            return toMovie(response, certification);
                })
                .toList();

        movieRepository.saveAll(movies);
    }

    // 카테고리 별 영화 목록 초기화
    public void initCategoryMovies() {
        log.info("initCategoryMovies() 카테고리 영화 초기 데이터 호출");
        if (movieCategoryRepository.count() > 0) {
            return;
        }
        refreshAllCategoryMovies();
    }

    // 카테고리 영화 목록 초기화(저장) 또는 업데이트
    private void refreshCategoryMovies(MovieCategoryType category, TmdbMovieList responses) {
        log.info("RefreshCategoryMovies 호출 : {}", category);
        // 기존 카테고리 영화 모두 삭제 (카테고리 목록들을 모두 삭제하고 다시 저장하는 방식으로 갱신한다)
        movieCategoryRepository.deleteByCategoryType(category);

        // 카테고리별 영화 새로 갱신
        for (TmdbMovie response: responses.results()) {
            log.info("refreshCategoryMovies 초기화 시작 : {}", category);
            Movie movie = saveOrUpdateMovie(response); // 영화 DB에 영화가 존재하면 갱신, 없다면 저장

            MovieCategory mc = MovieCategory.builder()
                    .categoryType(category)
                    .movie(movie)
                    .popularity(response.popularity())
                    .build();

            movieCategoryRepository.save(mc);
        }
    }

    // 스케줄링에 따른 카테고리 영화 초기화
    public void refreshAllCategoryMovies() {
        log.info("카테고리 영화 전체 갱신 시작");
        refreshCategoryMovies(MovieCategoryType.POPULAR, tmdbClient.getPopularMovies());
        refreshCategoryMovies(MovieCategoryType.UPCOMING, tmdbClient.getUpcomingMovies());
        refreshCategoryMovies(MovieCategoryType.NOW_PLAYING, tmdbClient.getNowPlayingMovies());
        log.info("카테고리 영화 전체 갱신 완료");
    }


    // TmdbMovie -> Movie 엔티티로 가공
    private Movie toMovie(TmdbMovie tmdbMovie, String certification) {
        return Movie.builder()
                .code(tmdbMovie.code())
                .title(tmdbMovie.title())
                .backdropPath(tmdbMovie.backdropPath())
                .posterPath(tmdbMovie.posterPath())
                .genres(getGenreNames(tmdbMovie.genres()))
                .certification(certification)
                .overview(tmdbMovie.overview())
                .releaseDate(tmdbMovie.releaseDate())
                .country(tmdbMovie.country())
                .runtime(tmdbMovie.runtime())
                .globalRating(tmdbMovie.globalRating())
                .detailFetched(false)
                .build();
    }

    // 영화 저장 또는 갱신
    private Movie saveOrUpdateMovie(TmdbMovie tmdbMovie) {
        String certification = extractCertification(tmdbMovie); // 관람 등급

        return movieRepository.findByCode(tmdbMovie.code()) // 영화 code에 해당하는 영화 찾기
                .map(movie -> {
                    log.info("saveOrUpdateMovie(): 영화 정보 갱신");
                    movie.updateFrom(tmdbMovie, certification); // 존재하면 영화 정보 업데이트(값들이 변경되어있을 수 있기때문)
                    return movie;
                })
                .orElseGet(() -> movieRepository.save(toMovie(tmdbMovie, certification))); // 존재하지 않으면 db 저장
    }

    // 장르 id에 대응하는 장르명 가져오기
    private List<String> getGenreNames(List<Integer> genres) {
        return genres.stream().map(genreId ->
                tmdbGenreRepository.findNameByGenreId(genreId)
        ).toList();
    }

    // 관람 등급 추출
    private String extractCertification(TmdbMovie tmdbMovie) {
        return tmdbClient.getMovieCertification(tmdbMovie.code()).results().stream()
                .filter(releaseData -> releaseData.iso31661().equals("KR")) // 한국만 추출
                .filter(releaseData -> releaseData.releaseDates() != null)
                .flatMap(releaseDates -> releaseDates.releaseDates().stream()) // release_dates[] 평탄화
                .filter(release -> release.certification() != null && !release.certification().isBlank())
                .filter(release -> release.type() == 3) // type==3 : 극장 개봉
                .findFirst()
                .map(info -> info.certification())
                .orElse("등급 미정");
    }

    // 더미 회원 생성
    public void createMember() {
        if (memberRepository.count() > 0)
            return;

        log.info("createMember(): 더미 멤버 생성 시작");
        List<Member> list = new ArrayList<>();

        for (int i = 9; i < 100; i++) {
            Member member = Member.builder()
                    .loginId("test" + i)
                    .password(passwordEncoder.encode("1234"))
                    .nickname("test" + i)
                    .email(null)
                    .role(RoleType.USER)
                    .providerType(ProviderType.LOCAL)
                    .build();
            list.add(member);
        }
        memberRepository.saveAll(list);
    }

    // 더미 코멘트 생성
    public void createComment() {
        if (commentRepository.count() > 0) return;

        log.info("createComment(): 코멘트 더미 데이터 생성 시작");
        Movie movie = movieRepository.findByCode(350L).get();

        List<Comment> list = new ArrayList<>();
        for (int i = 7; i < 100; i++) {
            Member member = memberRepository.findByLoginId("test" + i).get();
            Comment comment = Comment.builder()
                    .nickname(member.getNickname())
                    .content("test" + i)
                    .rating(ThreadLocalRandom.current().nextInt(1, 11) * 0.5)
                    .movie(movie)
                    .member(member)
                    .likeCount(0)
                    .build();
            list.add(comment);
        }
        commentRepository.saveAll(list);
    }

    public void calculateMovieAverage() {
//        commentRepository

    }
}
