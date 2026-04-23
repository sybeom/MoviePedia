package syb.moviepedia.member.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syb.moviepedia.common.exception.DuplicateSignupFieldException;
import syb.moviepedia.member.domain.CustomUserDetails;
import syb.moviepedia.member.domain.Member;
import syb.moviepedia.member.dto.MemberDto;
import syb.moviepedia.member.repository.MemberRepository;

import java.util.HashSet;
import java.util.Set;

@Service
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Long signup(MemberDto memberDto) {

        Set<String> errors = new HashSet<>(); // 회원 가입시 에러 메시를 담음
        if(memberRepository.existsByLoginId(memberDto.getLoginId())) { // true면 중복
            errors.add("loginId");
        }
        if (memberRepository.existsByNickname(memberDto.getNickname())) { // true면 중복
            errors.add("nickname");
        }

        if (!errors.isEmpty()) {
            throw new DuplicateSignupFieldException(errors, "이미 사용중인 필드입니다.");
        }

        Member member = Member.builder()
                .loginId(memberDto.getLoginId())
                .password(passwordEncoder.encode(memberDto.getPassword()))
                .nickname(memberDto.getNickname())
                .build();
        return memberRepository.save(member).getId();
    }

    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByLoginId(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        return CustomUserDetails.builder()
                .loginId(username)
                .password(member.getPassword())
                .build();
    }
}
