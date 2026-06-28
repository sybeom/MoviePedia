package syb.moviepedia.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import syb.moviepedia.jwt.repository.JwtRepository;
import syb.moviepedia.movie.service.MovieInitService;
import syb.moviepedia.tv.service.TVInitService;

import java.time.LocalDateTime;

/**
 * 주기적으로 기간이 만료된 Refresh 토큰은 제거하는 스케줄 구성 클래스
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ScheduleConfig {
    private final JwtRepository jwtRepository;
    private final MovieInitService movieInitService;
    private final TVInitService tvInitService;

    // TODO: https://sabarada.tistory.com/220 배치 하는 방법
    private int page = 1;

    // 새벽 3시마다 Refresh 토큰 저장소 8일 지난 토큰 삭제
    @Scheduled(cron = "0 0 3 * * *") // 기본 cron = "0 0 3 * * *" (새벽 3시 진행)
    public void JwtRefreshSchedule() {
        log.info("Refresh Schedule 호출");
        LocalDateTime cutoff = LocalDateTime.now().minusDays(8);
        jwtRepository.deleteByCreatedDateBefore(LocalDateTime.now());
    }
//    // 매일 새벽 4시 카테고리 영화 갱신 fixedRate = 1000000, initialDelay = 10000
//    @Scheduled(fixedRate = 15000, initialDelay = 10000) // 3분마다 삽입
//    public void InsertMovie() {
////        movieInitService.initMovies(page++);
//        tvInitService.initTV(page++);
//    }

    // 매일 새벽 4시 카테고리 영화 갱신 fixedRate = 1000000, initialDelay = 10000
    @Scheduled(cron = "0 0 3 * * *") // initialDelay는 서버 시작후 해당 시간 뒤에 실행된다는 의미
    public void movieCategoryRefreshSchedule() {
        log.info("카테고리 영화 갱신 스케줄 호출");

        movieInitService.refreshAllCategoryMovies();
    }
//
}
