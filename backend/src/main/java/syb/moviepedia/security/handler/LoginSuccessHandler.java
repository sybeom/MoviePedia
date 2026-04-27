package syb.moviepedia.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import syb.moviepedia.common.api.ApiResult;
import syb.moviepedia.common.util.JwtUtil;
import syb.moviepedia.jwt.service.JwtService;
import syb.moviepedia.member.domain.CustomUserDetails;
import syb.moviepedia.member.dto.request.MemberLoginResponseDto;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * 로그인 성공시 실행되는 핸들러
 * SecurityConfig에 등록해야 동작
 */
@Slf4j
@RequiredArgsConstructor
@Component
@Qualifier("LoginSuccessHandler")
public class LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtService jwtService;

    /**
     * 로그인 필터에서 로그인 성공(successfulAuthentication()) 시 실행
     * 로그인 성공시 프론트에 JWT 발급
     * @param response 요청에 대한 응답
     * @param authentication 로그인 후 넘어온 객체이므로 로그인 성공 유저에 대한 정보가 있음
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        // loginId, role, nickname
        String loginId =  authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        String nickname = principal.getNickname();

        // JWT(Access/Refresh) 생성
        String accessToken = JwtUtil.createJWT(loginId, role, true);
        String refreshToken = JwtUtil.createJWT(loginId, role, false);

        // 발급한 Refresh DB 테이블 저장 (Refresh whitelist)
        jwtService.save(loginId, refreshToken);

        // 응답
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        String json = generateJsonString(loginId, nickname, accessToken, refreshToken); // 데이터 공통 응답 형식으로 변경
        response.getWriter().write(json);
        response.getWriter().flush();
    }
    
    // 로그인 데이터, 닉네임, 액세스 및 리프레쉬 토큰 Json 문자열화
    public String generateJsonString(String loginId, String nickname, String accessToken, String refreshToken) {
        MemberLoginResponseDto loginResponseDto = MemberLoginResponseDto.builder()
                .loginId(loginId)
                .nickname(nickname)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return new ObjectMapper().writeValueAsString(ApiResult.success("로그인 성공", loginResponseDto));
    }
}
