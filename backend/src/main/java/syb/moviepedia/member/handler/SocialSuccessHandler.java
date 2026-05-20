package syb.moviepedia.member.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import syb.moviepedia.common.util.JwtUtil;
import syb.moviepedia.jwt.service.JwtService;

import java.io.IOException;

/**
 * 소셜 로그인 완료 이후 실행 되는 로그인 성공 핸들러
 */
@Component
@Slf4j
@Qualifier("SocialSuccessHandler")
public class SocialSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;

    public SocialSuccessHandler(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * 소셜 로그인 성공하면 실행
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        log.info("소셜 로그인 성공 핸들러 실행");
        log.info("request URI = {}", request.getRequestURI());
        log.info("authentication = {}", authentication);
        // username, role
        String username =  authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        // JWT(Refresh) 발급 - 소셜 로그인은 쿠키 방식으로만 응답할 수 있기때문에 Refresh Token만 먼저 발급 받는다.
        String refreshToken = JwtUtil.createJWT(username, "ROLE_" + role, false);

        // 발급한 Refresh DB 테이블 저장 (Refresh whitelist)
        jwtService.save(username, refreshToken);

        // 응답 - 소셜 로그인 방식은 쿠키로 밖에 응답하지 못한다.
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken); // JwtService에서 쓰인다.
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(10); // 10초 (프론트에서 발급 후 바로 헤더 전환 로직 진행 예정)

        response.addCookie(refreshCookie);
        // 이 주소는 소셜 로그인 성공 후 백엔드에서 응답이 오면 쿠키를 받아서 쿠키를 다시 헤더로 바꿔달라고 요청을 하기 위한 프론트 페이지.
        // sendRedirect는 302 상태 코드로 응답을 보내고 Location 헤더에 해당 url을 넣는다.
        // 웹 브라우저는 3xx 응답의 결과에 Location 헤더가 있으면, Location 위치로 자동 이동한다 (리다이렉트)
        response.sendRedirect("http://localhost:5173/cookie");
    }
}
