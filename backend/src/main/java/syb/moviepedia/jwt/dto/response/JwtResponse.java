package syb.moviepedia.jwt.dto.response;

/**
 * 자체, 소셜 로그인 등 결과로 생성된 jwt를 프론트 측에 보내기 위한 응답 DTO
 */
public record JwtResponse(String loginId, String nickname, String accessToken, String refreshToken) {
}
