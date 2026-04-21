package syb.moviepedia.jwt.dto;

/**
 * 자체, 소셜 로그인 등 결과로 생성된 jwt를 프론트 측에 보내기 위한 DTO
 */
public record JwtDto(String loginId, String nickname, String accessToken, String refreshToken) {
}
