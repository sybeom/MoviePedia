package syb.moviepedia.security.oauth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

/**
 * 소셜 로그인 과정중 loadUser()에서 반환하는 객체
 * 이 객체는 Authentication의 principal에 들어간다.
 */
public class OAuth2MemberPrincipal implements OAuth2User {

    private final Map<String, Object> attributes;
    private final Collection<? extends GrantedAuthority> authorities;
    private final String loginId;

    public OAuth2MemberPrincipal(Map<String, Object> attributes,
                                 Collection<? extends GrantedAuthority> authorities,
                                 String loginId) {
        this.attributes = attributes;
        this.authorities = authorities;
        this.loginId = loginId;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return loginId;
    }

}
