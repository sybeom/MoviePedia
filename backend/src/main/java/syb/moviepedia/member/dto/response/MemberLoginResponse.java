package syb.moviepedia.member.dto.response;

import lombok.Builder;

/**
 * 로그인 응답 DTO
 */
@Builder
public record MemberLoginResponse(
        String loginId,
        String nickname,
        String accessToken,
        String refreshToken
) {
}
