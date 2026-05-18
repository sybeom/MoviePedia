package syb.moviepedia.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StreamUtils;
import syb.moviepedia.common.api.ApiResult;
import syb.moviepedia.common.api.ErrorCode;
import syb.moviepedia.member.dto.request.MemberLoginResponseDto;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 자체 로그인을 위한 필터
 * POST /login으로 요청이 오면 실행된다
 */
@Slf4j
public class LoginFilter extends AbstractAuthenticationProcessingFilter {

    public static final String SPRING_SECURITY_FORM_USERNAME_KEY = "loginId";
    public static final String SPRING_SECURITY_FORM_PASSWORD_KEY = "password";
    private static final RequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER = PathPatternRequestMatcher.withDefaults()
            .matcher(HttpMethod.POST, "/login");

    private String usernameParameter = SPRING_SECURITY_FORM_USERNAME_KEY;
    private String passwordParameter = SPRING_SECURITY_FORM_PASSWORD_KEY;
    private final AuthenticationSuccessHandler authenticationSuccessHandler;

    public LoginFilter(AuthenticationManager authenticationManager, AuthenticationSuccessHandler authenticationSuccessHandler) {
        super(DEFAULT_ANT_PATH_REQUEST_MATCHER, authenticationManager);
        this.authenticationSuccessHandler = authenticationSuccessHandler; // SecurityConfig에서 주입된다
    }

    @Override
    public @Nullable Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        Map<String, String> loginMap;
        try { // 메시지 바디 json 변환 및 데이터를 객체 형태로 변환
            ObjectMapper objectMapper = new ObjectMapper(); // json <-> 자바 객체 변환기
            ServletInputStream inputStream = request.getInputStream(); // 메시지 바디를 바이트 코드 형태로 얻는다.
            String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8); // 바이트 코드를 String 형태로 변환 -> json 형태이다
            loginMap = objectMapper.readValue(messageBody, new TypeReference<>() {}); // json 문자열을 자바 객체로 변환
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String loginId = loginMap.get(usernameParameter).trim();
        String password = loginMap.get(passwordParameter).trim();

        log.info("LoginFilter 호출");

        /**
         * UsernamePasswordAuthenticationToken : 인증 요청 토큰 생성
         * Principal : 유저에 대한 정보
         * Credentials : 증명 (비밀번호, 토큰)
         * Authorities : 유저의 권한(ROLE) 목록
         */
        UsernamePasswordAuthenticationToken authRequest = UsernamePasswordAuthenticationToken.unauthenticated(loginId, password);
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));

        return this.getAuthenticationManager().authenticate(authRequest);
    }

    // 로그인 성공시 어떤걸 수행할지 물어보는 메서드
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        log.info("successfulAuthentication 호출");
        // 로그인 성공시 LoginSuccessHandler를 통해 프론트에 JWT를 발급한다.
        authenticationSuccessHandler.onAuthenticationSuccess(request, response, authResult);
    }

    // 로그인 실패시 수행되는 메서드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        String json = generateJsonBody();
        response.getWriter().write(json);
    }

    // 응답 데이터 json화
    public String generateJsonBody() {
        Set<String> errors = new HashSet<>();
        errors.add("loginId");
        errors.add("password");

        return new ObjectMapper().writeValueAsString(ApiResult.fail(ErrorCode.LOGIN_FAILED, "아이디 또는 비밀번호가 올바르지 않습니다.", errors));
    }
}
