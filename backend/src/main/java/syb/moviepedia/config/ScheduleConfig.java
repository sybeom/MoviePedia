package syb.moviepedia.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import syb.moviepedia.jwt.repository.JwtRepository;

import java.time.LocalDateTime;

/**
 * 주기적으로 기간이 만료된 Refresh 토큰은 제거하는 스케줄 구성 클래스
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ScheduleConfig {
    private final JwtRepository jwtRepository;

    // 새벽 3시마다 Refresh 토큰 저장소 8일 지난 토큰 삭제
    @Scheduled(fixedRate = 10000)
    public void JwtRefreshSchedule() {
        log.info("Refresh Schedule 호출");
        LocalDateTime cutoff = LocalDateTime.now().minusDays(8);
        jwtRepository.deleteByCreatedDateBefore(LocalDateTime.now());
    }
}
