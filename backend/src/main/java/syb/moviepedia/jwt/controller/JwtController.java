package syb.moviepedia.jwt.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import syb.moviepedia.jwt.dto.JwtDto;
import syb.moviepedia.jwt.dto.JwtRefreshRequestDto;
import syb.moviepedia.jwt.service.JwtService;

/**
 * 쿠키에 실어보낸 리프레쉬 토큰을 다시 헤더 방식으로 변경하거나
 * 토큰 만료 재발급을 다루는 클래스
 */
@Tag(name="JWT API", description = "JWT 도메인 API")
@Slf4j
@RestController
public class JwtController {

    private final JwtService jwtService;

    public JwtController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    // 소셜 로그인 쿠키 방식의 Refresh 토큰 헤더 방식으로 교환
    @Operation(
            summary = "쿠키 -> JWT 변환",
            description = "소셜 로그인의 쿠키를 JWT 방식으로 변환한다."
    )
    @PostMapping(value = "/jwt/exchange")
    public JwtDto jwtExchangeApi(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        log.info("JwtController 호출됨. uri: /jwt/exchange");
        return jwtService.cookieToHeader(request, response);
    }

    // 액세스 토큰 만료시 재발급
    // Refresh 토큰으로 Access 토큰 재발급 (Rotate 포함)
    @Operation(
            summary = "access 토큰 재발급",
            description = "access 토큰 만료시 refresh 토큰을 사용해 재발급한다"
    )
    @PostMapping(value = "/jwt/refresh")
    public JwtDto jwtRefreshApi(
            @Validated @RequestBody JwtRefreshRequestDto dto
    ) {
        log.info("JwtController 호출됨. uri: /jwt/refresh");
        return jwtService.refreshRotate(dto);
    }

}
