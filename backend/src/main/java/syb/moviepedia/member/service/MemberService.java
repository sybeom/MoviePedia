package syb.moviepedia.member.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syb.moviepedia.common.ProviderType;
import syb.moviepedia.common.RoleType;
import syb.moviepedia.common.exception.DuplicateSignupFieldException;
import syb.moviepedia.security.user.CustomUserDetails;
import syb.moviepedia.member.domain.Member;
import syb.moviepedia.member.dto.SocialMemberDto;
import syb.moviepedia.security.oauth.OAuth2MemberPrincipal;
import syb.moviepedia.member.dto.request.MemberSignupRequest;
import syb.moviepedia.member.repository.MemberRepository;

import java.util.*;

@Slf4j
@Service
public class MemberService extends DefaultOAuth2UserService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void signup(MemberSignupRequest memberDto) {

        Set<String> errors = new HashSet<>(); // 회원 가입시 에러 메시를 담음
        if(memberRepository.existsByLoginId(memberDto.loginId())) { // true면 중복
            errors.add("loginId");
        }
        if (memberRepository.existsByNickname(memberDto.nickname())) { // true면 중복
            errors.add("nickname");
        }

        if (!errors.isEmpty()) {
            throw new DuplicateSignupFieldException(errors, "이미 사용중인 필드입니다.");
        }

        Member member = Member.builder()
                .loginId(memberDto.loginId())
                .password(passwordEncoder.encode(memberDto.password()))
                .nickname(memberDto.nickname())
                .role(RoleType.USER)
                .providerType(ProviderType.LOCAL)
                .build();
        memberRepository.save(member).getId();
    }

    @Transactional(readOnly = true)
    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByLoginId(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        return CustomUserDetails.builder()
                .id(member.getId())
                .loginId(username)
                .password(member.getPassword())
                .nickname(member.getNickname())
                .build();
    }

    // 소셜 회원가입 및 로그인
    @Transactional
    @Override // Oauth2 관련 빈이 유저 정보를 받으면 loadUser()를 호출해 네이버로부터 받은 유저 정보를 객체를 userRequest에 넣어줌.
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("loadUser 호출 확인");
        // 부모 메소드 호출. 받은 유저 정보 (userRequest를 파싱한다.)
        OAuth2User oAuth2User = super.loadUser(userRequest); // 파싱된 유저 정보가 들어있음

        // 파싱된 유저 정보에서 데이터를 파싱하여 담을 변수들
        Map<String, Object> attributes;
        List<GrantedAuthority> authorities;

        String loginId;
        RoleType role = RoleType.USER;
        String email;
        String nickname;

        // provider 제공자별 데이터 획득 - 네이버, 구글 등 제공자 별 데이터를 제공하는 방식이 달라 파싱 방법도 달라진다.
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        ProviderType provider = ProviderType.from(registrationId); // registrationId에 해당하는 enum 상수 객체 반환

        if (provider==ProviderType.NAVER) { // 네이버
            attributes = (Map<String, Object>) oAuth2User.getAttributes().get("response");
            loginId = provider.name() + "_" + attributes.get("id");
            email = attributes.get("email").toString();
            nickname = attributes.get("nickname").toString(); // OAuth2User

        } else if (provider== ProviderType.GOOGLE) { // 구글

            attributes = (Map<String, Object>) oAuth2User.getAttributes();
            loginId = provider.name() + "_" + attributes.get("sub");
            email = attributes.get("email").toString();
            nickname = attributes.get("name").toString();
        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다.");
        }
        // 로그인 타입이 LOCAL이 아닌 것 찾기
        Optional<Member> member = memberRepository.findByLoginIdAndProviderTypeNot(loginId, ProviderType.LOCAL);
        // 데이터베이스 조회 -> 존재하면 업데이트, 없으면 신규 가입
        if (member.isPresent()) { // 변경 데이터 있을시 덮어 씌우기 위함. 예) 닉네임 등
            // role 조회
//            role = member.get().getRole().name();
            // 기존 유저 업데이트
            SocialMemberDto memberDto = new SocialMemberDto(nickname, email);
            member.get().update(memberDto);

            memberRepository.save(member.get());
        } else { // 없으면 신규 유저로 추가
            Member newMember = Member.builder()
                    .loginId(loginId)
                    .password("")
                    .providerType(provider)
                    .role(role)
                    .nickname(nickname)
                    .email(email)
                    .build();

            memberRepository.save(newMember);
        }

        authorities = List.of(new SimpleGrantedAuthority("USER"));

        return new OAuth2MemberPrincipal(attributes, authorities, loginId);
    }
}
