package syb.moviepedia.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

/**
 * 회원가입 요청 dto
 */
@Builder
public record MemberSignupRequest(
        @Schema(description = "로그인 아이디", example = "user123")
        @NotBlank(message = "로그인 아이디는 필수입니다.")
        @Pattern(regexp = "^[a-zA-Z0-9]+$")
        @Size(min = 5, max = 10)
        String loginId,

        @Schema(description = "비밀번호", example = "qwer1234!")
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 2, max = 10)
        String password,

        @Schema(description = "닉네임", example = "홍길동")
        @NotBlank(message = "닉네임은 필수입니다.")
        @Pattern(regexp = "^[A-Za-z0-9가-힣ㄱ-ㅎ]+$")
        @Size(min = 2, max = 6)
        String nickname
) {}
