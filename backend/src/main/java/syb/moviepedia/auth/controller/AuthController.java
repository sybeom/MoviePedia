package syb.moviepedia.auth.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import syb.moviepedia.common.api.ApiResult;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/me") // 홈 화면 진입시 로그인 상태 판별을 위해 토큰 검증을 하는 요청
    public ResponseEntity<ApiResult<Void>> checkMe() {
        log.info("AuthController: /auth/me 요청 도달");
        return ResponseEntity.ok().body(ApiResult.success("토큰 인증에 성공하였습니다"));
    }
}
