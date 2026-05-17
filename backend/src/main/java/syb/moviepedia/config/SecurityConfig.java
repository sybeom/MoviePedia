package syb.moviepedia.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import syb.moviepedia.jwt.service.JwtService;
import syb.moviepedia.member.handler.RefreshTokenLogoutHandler;
import syb.moviepedia.security.filter.JwtFilter;
import syb.moviepedia.security.filter.LoginFilter;

@EnableWebSecurity
@Configuration
public class SecurityConfig {
    private final AuthenticationConfiguration authenticationConfiguration;
    private final AuthenticationSuccessHandler loginSuccessHandler;
    private final AuthenticationSuccessHandler socialSuccessHandler;
    private final JwtService jwtService;

    // @RequiredArgsConstructor 사용해도 될듯
    public SecurityConfig(
            AuthenticationConfiguration authenticationConfiguration,
            @Qualifier("LoginSuccessHandler") AuthenticationSuccessHandler loginSuccessHandler,
            @Qualifier("SocialSuccessHandler") AuthenticationSuccessHandler socialSuccessHandler, JwtService jwtService) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.loginSuccessHandler = loginSuccessHandler;
        this.socialSuccessHandler = socialSuccessHandler;
        this.jwtService = jwtService;
    }

    // 비밀번호 암호화용 Bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 커스텀 자체 로그인 필터를 위한 AuthenticationManager Bean 수동 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Vite 개발 서버 출처 허용 설정
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization", "Set-Cookie"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 프론트엔드 fetch 요청 허용 설정
        // 수 많은 필터 중 CSRF 필터를 disable 시킴
        http
                .csrf(csrf -> csrf.disable());

        // CORS 설정
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // 기본 로그아웃 필터 + 커스텀 Refresh 토큰 삭제 핸들러 추가. (로그아웃 필터는 자동 활성화 되어있음)
        // 기본 로그아웃 필터가 적용되는 경로는 POST /logout
        http
                .logout(logout -> logout
                        // logoutSuccessHandler() : 로그아웃 성공시 상태코드만 반환하도록 변경. 기본 설정은 302 Found + Location으로 리다이렉트였음.
                        // 기본 리다이렉트 경로는 /login?logout. 즉 로그아웃 성공시 자동으로 /login?logout으로 리다이렉트 함.
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK))
                        .addLogoutHandler(new RefreshTokenLogoutHandler(jwtService))); // 리프레쉬 토큰 삭제 핸들러

        // OAuth2 인증용
        http
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(socialSuccessHandler));

        // 인가 설정
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/auth/me").authenticated() // 로그인 여부 확인
                        .requestMatchers(HttpMethod.POST, "/movies/{movieId}/comments").authenticated() // 코멘트 작성
                        .anyRequest().permitAll());

        // 커스텀 필터 추가
        http
                .addFilterBefore(new JwtFilter(), LogoutFilter.class);

        // 커스텀 필터 추가
        http
                .addFilterBefore(
                        new LoginFilter(authenticationManager(authenticationConfiguration), loginSuccessHandler),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
