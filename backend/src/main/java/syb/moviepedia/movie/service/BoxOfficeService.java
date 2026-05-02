package syb.moviepedia.movie.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import syb.moviepedia.movie.dto.WeeklyBoxOfficeDto;
import syb.moviepedia.movie.external.kmdb.KmdbClient;
import syb.moviepedia.movie.external.kobis.KobisBoxOfficeInfo;
import syb.moviepedia.movie.external.kobis.KobisClient;
import syb.moviepedia.movie.external.tmdb.TmdbClient;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class BoxOfficeService {

    private final KobisClient kobisClient;
    private final KmdbClient kmdbClient;
    private final TmdbClient tmdbClient;

    /**
     * 영화진흥위원회 API 호출 -> 박스 오피스 json 파싱 후 정보(제목, 순위) 가져옴 -> 제목 기반(쿼리파라미터) kmdb api 호출
     */
    public List<WeeklyBoxOfficeDto> getBoxOfficeWeekly() {

        List<KobisBoxOfficeInfo> kobisBoxOfficeInfos = kobisClient.fetchWeeklyBoxOffice();
        return kobisBoxOfficeInfos.stream()
                .map(movie ->
                        kmdbClient.fetchKmdbMovie(movie.movieName())) // 영화 하나당 api 한번 호출
                .toList();
    }

    public String getMovies() {
        return tmdbClient.getPopularMovies();
    }
}
