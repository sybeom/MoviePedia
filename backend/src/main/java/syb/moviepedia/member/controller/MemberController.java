package syb.moviepedia.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import syb.moviepedia.common.api.ApiResult;
import syb.moviepedia.member.dto.MemberDto;
import syb.moviepedia.member.service.MemberService;

@Tag(name = "Member API", description = "회원 도메인 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    final private MemberService memberService;

    @Operation(
            summary = "회원 가입",
            description = "회원 가입을 진행합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "가입할 회원 JSON Body 데이터",
                    required = true,
                    content = @Content( // 요청 데이터 타입
                            schema = @Schema(implementation = MemberDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content =  @Content( // 성공 응답 데이터 타입
                                    schema = @Schema(implementation = MemberDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "실패"
                    )
            }
    )
    @PostMapping
    public ResponseEntity<ApiResult<Void>> signup(@Valid @RequestBody MemberDto dto) {

//        // 검증 실패시
//        // 필드 입력 조건에 대한 검증을 하는 것임. 아이디, 비밀번호, 닉네임 등의 중복 체크는 하지 않음)
//        if (bindingResult.hasErrors()) {
//            Set<String> errors = new HashSet<>(); // 필드명, 에러코드 작성
//            bindingResult.getFieldErrors().forEach(error -> {
//                log.info("error: " + error);
//                errors.add(error.getField()); // 에러 필드명과 필드에 대한 기본 에러 메시지 넣음
//            });
//            // 400 응답. 프론트에서 처리.
//            return ResponseEntity.badRequest().build();
//        }
        log.info("signup 호출");
        // 중복 검사 및 가입
        memberService.signup(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.success("회원 가입에 성공했습니다", null));
    }
}
