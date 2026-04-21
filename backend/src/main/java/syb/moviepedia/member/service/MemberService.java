package syb.moviepedia.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syb.moviepedia.common.exception.SignupFieldException;
import syb.moviepedia.member.domain.Member;
import syb.moviepedia.member.dto.MemberDto;
import syb.moviepedia.member.repository.MemberRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
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
}
