package syb.moviepedia.member.domain;

import lombok.Builder;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 로그인 검증 과정에서 로그인 입력 데이터들과 비교해 검증할 클래스이다.
 * 검증 완료 후엔 Authentication의 principal에 담긴다.
 */
@Builder
@Getter
public class CustomUserDetails implements UserDetails {
    private Long id;
    private String loginId;
    private String password;
    private String nickname;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return loginId;
    }
}
