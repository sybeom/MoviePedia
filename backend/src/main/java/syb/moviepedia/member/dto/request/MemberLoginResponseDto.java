package syb.moviepedia.member.dto.request;

import lombok.Builder;

/**
 * 로그인 응답 DTO 클래스
 */
@Builder
public record MemberLoginResponseDto(
        String loginId,
        String nickname,
        String accessToken,
        String refreshToken
) {
}
