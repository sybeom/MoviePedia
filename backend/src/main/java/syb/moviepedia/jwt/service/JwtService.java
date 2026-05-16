package syb.moviepedia.jwt.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syb.moviepedia.common.exception.InvalidRefreshTokenException;
import syb.moviepedia.common.exception.RefreshTokenCookieNotFoundException;
import syb.moviepedia.common.util.JwtUtil;
import syb.moviepedia.jwt.domain.JwtRefresh;
import syb.moviepedia.jwt.dto.JwtRefreshRequestDto;
import syb.moviepedia.jwt.dto.JwtResponseDto;
import syb.moviepedia.jwt.repository.JwtRepository;
import syb.moviepedia.member.repository.MemberRepository;

import java.util.Arrays;
import java.util.Optional;

/**
 * jwt 비즈니스 로직을 수행하는 jwt 서비스 클래스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtRepository jwtRepository;
    private final MemberRepository memberRepository;

    // 소셜 로그인은 로그인 성공 후 쿠키(Refresh) -> 헤더 방식으로 응답
    // 소셜은 RESTFul하게 설계를 하게되면 쿠키 형태로 토큰을 발급해줘야한다.
    // 따라서 이 쿠키를 다시 헤더 방식으로 변경해야줘야한다.
    @Transactional
    public JwtResponseDto cookieToHeader( // 소셜 로그인 성공 후 쿠키로 발급받은 jwt를 다시 헤더로 발급
                                          HttpServletRequest request,
                                          HttpServletResponse response
    ) {
        // 쿠키에서 리프레쉬 토큰 획득
        Cookie[] cookies = request.getCookies();
        String refreshToken = Arrays.stream(Optional.ofNullable(cookies)
                        .orElse(new Cookie[0])) // 쿠키가 없으면 빈 배열을 던진다. 빈 쿠키 배열을 던지면 어쨌건 예외가 발생하는건 똑같다.
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .map(cookie -> cookie.getValue())
                .findFirst()
                .orElseThrow(() ->
                        new RefreshTokenCookieNotFoundException("refreshToken 쿠키가 없습니다.")
                );

        // Refresh 토큰 검증
        Boolean isValid = JwtUtil.validateToken(refreshToken, false);
        if (!isValid) {
            throw new InvalidRefreshTokenException("유효하지 않은 refreshToken입니다.");
        }

        // 정보 추출
        String loginId = JwtUtil.getLoginId(refreshToken);
        String role = JwtUtil.getRole(refreshToken);
        String nickname = memberRepository.findNicknameByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 유저를 찾을 수 없습니다: " + loginId));

        // 토큰 생성
        String newAccessToken = JwtUtil.createJWT(loginId, role, true);
        String newRefreshToken = JwtUtil.createJWT(loginId, role, false);

        // 기존 Refresh 토큰 DB 삭제 후 신규 추가
        JwtRefresh newRefreshEntity = JwtRefresh.builder()
                .loginId(loginId)
                .refreshToken(newRefreshToken)
                .build();
        removeRefresh(refreshToken);
        jwtRepository.flush(); // 같은 트랜잭션 내부라 : 삭제 -> 생성 문제 해결
        jwtRepository.save(newRefreshEntity);

        // 기존 쿠키 제거
        Cookie refreshCookie = new Cookie("refreshToken", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(10);
        response.addCookie(refreshCookie);

        return new JwtResponseDto(loginId, nickname, newAccessToken, newRefreshToken);
    }

    // Refresh 토큰으로 Access 토큰 재발급 로직 (Rotate 포함)
    @Transactional
    public JwtResponseDto refreshRotate(JwtRefreshRequestDto dto) {
        String refreshToken = dto.getRefreshToken();

        // Refresh 토큰 검증
        Boolean isValid = JwtUtil.validateToken(refreshToken, false);
        if (!isValid) {
            log.info("refreshRotate(): refreshToken 검증 실패");
            throw new InvalidRefreshTokenException("유효하지 않은 refreshToken입니다.");
        }

        // RefreshEntity DB 존재 확인 (화이트리스트)
        if (!existsRefresh(refreshToken)) {
            log.info("refreshRotate(): refreshToken DB 존재하지 않음");
            throw new InvalidRefreshTokenException("유효하지 않은 refreshToken입니다.");
        }

        /**
         * 리프레쉬 토큰이 만료되지 않았다면, 리프레쉬 토큰을 사용해 액세스 토큰을 재발급한다.
         */
        // 정보 추출
        String loginId = JwtUtil.getLoginId(refreshToken);
        String role = JwtUtil.getRole(refreshToken);
        String nickname = memberRepository.findNicknameByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 아이디를 찾을 수 없습니다: " + loginId));

        // 토큰 생성
        String newAccessToken = JwtUtil.createJWT(loginId, role, true);
        String newRefreshToken = JwtUtil.createJWT(loginId, role, false);

        // 기존 Refresh 토큰 DB 삭제 후 신규 추가
        JwtRefresh newRefreshEntity = JwtRefresh.builder()
                .loginId(loginId)
                .refreshToken(newRefreshToken)
                .build();
        removeRefresh(refreshToken);
        jwtRepository.save(newRefreshEntity);

        return new JwtResponseDto(loginId, nickname, newAccessToken, newRefreshToken);
    }

    // JWT Refresh 토큰 발급 후 DB 저장
    @Transactional
    public void save(String loginId, String refreshToken) {
        JwtRefresh entity = JwtRefresh.builder()
                .loginId(loginId)
                .refreshToken(refreshToken)
                .build();

        jwtRepository.save(entity);
    }

    // JWT Refresh 존재 확인
    @Transactional(readOnly = true)
    public Boolean existsRefresh(String refreshToken) {
        return jwtRepository.existsByRefreshToken(refreshToken);
    }

    // JWT Refresh 토큰 삭제
    @Transactional
    public void removeRefresh(String refreshToken) {
        jwtRepository.deleteByRefreshToken(refreshToken);
    }

    // 특정 유저 Refresh 토큰 모두 삭제 (탈퇴)
    @Transactional
    public void removeRefreshUser(String loginId) {
        jwtRepository.deleteByLoginId(loginId);
    }

}
