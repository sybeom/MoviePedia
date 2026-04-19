package syb.moviepedia.member.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import syb.moviepedia.member.domain.Member;
import syb.moviepedia.member.dto.MemberDto;
import syb.moviepedia.member.service.MemberService;

@RestController
@RequiredArgsConstructor
public class MemberController {

    final private MemberService memberService;

    @GetMapping("/")
    public String index() {
        MemberDto dto = new MemberDto("test", "1234");
        memberService.signup(dto);
        return "Hello World";
    }
}
