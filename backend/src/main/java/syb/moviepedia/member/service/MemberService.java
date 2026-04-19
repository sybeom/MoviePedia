package syb.moviepedia.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        Member member = new Member(memberDto.getLoginId(), memberDto.getPassword());
        return memberRepository.save(member).getId();
    }
}
