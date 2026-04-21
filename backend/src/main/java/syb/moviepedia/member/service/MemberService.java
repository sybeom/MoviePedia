package syb.moviepedia.member.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syb.moviepedia.common.exception.SignupFieldException;
import syb.moviepedia.member.domain.Member;
import syb.moviepedia.member.dto.MemberDto;
import syb.moviepedia.member.repository.MemberRepository;

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
        if (memberRepository.existsByLoginId(memberDto.getLoginId())) {
            throw new SignupFieldException("중복 된 아이디입니다.");
        }

        if (memberRepository.existsByNickname(memberDto.getNickname())) {
            throw new SignupFieldException("중복 된 닉네임입니다.");
        }

        Member member = Member.builder()
                .loginId(memberDto.getLoginId())
                .password(memberDto.getPassword())
                .nickname(memberDto.getNickname())
                .build();
        return memberRepository.save(member).getId();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return User.builder()
                .username("test1234")
                .password(passwordEncoder.encode("1234"))
                .build();
    }
}
