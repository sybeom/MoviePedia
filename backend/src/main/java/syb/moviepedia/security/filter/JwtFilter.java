package syb.moviepedia.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import syb.moviepedia.common.util.JwtUtil;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * 권한이 요구되는 API에 접근하게 된다면 로그인 후 발급한 JWT를 헤더에 지참해야한다.
 * 그 헤더에 실려온 Jwt 검증하는 필터
 * JWT(Access)를 검증하여 쓰레드에 배정되는 SecurityContext에 username과 role을 부여하면 된다.
 * 항상 실행된다. Jwt 유효성 검증 결과에 차이가 있을 뿐.
 */
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("JwtFilter 호출 : {}", request.getRequestURI());
        // 액세스 토큰 재발급을 위한 /jwt/refresh 요청에는 Authorization 헤더는 없고, json 바디에 리프레쉬 토큰만 있으므로
        // authorization을 통과 못하고 return 된다. return되고 Controller로 요청이 넘어간다
        String authorization = request.getHeader("Authorization"); // 요청 헤더에서 jwt를 가져옴
        if (authorization == null || authorization.isBlank()) { // 없으면 다음 필터로 넘어감 예) 비로그인으로 게시글 볼 경우
            filterChain.doFilter(request, response);
            return;
        }
        log.info("JwtFilter Bearer 검사 직전 호출");
        if (!authorization.startsWith("Bearer ")) { // jwt가 있지만 Bearer라는 접두사가 없으면 에러
            throw new ServletException("Invalid JWT token");
        }

        // 문제 없으면 토큰 파싱 진행
        String accessToken = authorization.split(" ")[1];
        log.info("accessToken {}", accessToken);
        log.info("JwtFilter 액세스 토큰 검증 직전, {}", request.getRequestURI());

        if (JwtUtil.validateToken(accessToken, true)) { // jwt 검증

            log.info("HTTP 메소드: {}, 요청 URI: {} 액세스 토큰 검증 성공", request.getMethod(), request.getRequestURI());

            String username = JwtUtil.getLoginId(accessToken);
            String role = JwtUtil.getRole(accessToken); // jwt를 발급 할 때 role을 넣었으므로 꺼낼 수 있음.

            List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));

            /**
             * UsernamePasswordAuthenticationToken의 파라미터들
             * Principal : 유저에 대한 정보
             * Credentials : 증명 (비밀번호, 토큰)
             * Authorities : 유저의 권한(ROLE) 목록
             */
            Authentication auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
            // 넣은 Authentication을 AuthorizationFilter에서 꺼내어 hasRole값과 비교한다.
            // username과 권한(role)만 넣었으므로 Authentication으로 username과 권한만 꺼낼 수 있다
            SecurityContextHolder.getContext().setAuthentication(auth);

            filterChain.doFilter(request, response);

        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 만료시 401 응답
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"토큰 만료 또는 유효하지 않은 토큰\"}");
            return;
        }
    }
}
