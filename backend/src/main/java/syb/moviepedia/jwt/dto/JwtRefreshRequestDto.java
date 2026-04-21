package syb.moviepedia.jwt.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/**
 * 리프레쉬 토큰을 받기 위한 DTO
 * 액세스 토큰 재발급을 위해 그에 필요한 리프레쉬 토큰을 전달받음
 */
@Getter
public class JwtRefreshRequestDto {

    @NotBlank
    private String refreshToken;
}
