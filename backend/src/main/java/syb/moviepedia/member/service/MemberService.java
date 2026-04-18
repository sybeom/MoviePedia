package syb.moviepedia.member.service;

import org.springframework.stereotype.Service;
import syb.moviepedia.member.domain.Member;
import syb.moviepedia.member.repository.MemberRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public void save(Member member) {
        memberRepository.save(member);
    }
}
