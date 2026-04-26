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
import syb.moviepedia.common.swagger.SwaggerApiResponse;
import syb.moviepedia.common.swagger.SwaggerFailResponse;
import syb.moviepedia.member.dto.request.MemberSignupRequestDto;
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
            description = "회원 가입을 진행한다",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "가입할 회원 JSON Body 데이터",
                    required = true,
                    content = @Content( // 요청 데이터 타입
                            schema = @Schema(implementation = MemberSignupRequestDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "성공",
                            content = @Content(
                                    schema = @Schema(implementation = SwaggerApiResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "실패",
                            content = @Content(
                                    schema =  @Schema(implementation = SwaggerFailResponse.class)
                            )
                    )
            }
    )
    @PostMapping
    public ResponseEntity<ApiResult<Void>> signup(
            @Valid
            @RequestBody
            MemberSignupRequestDto dto) {

        log.info("signup 호출");
        // 중복 검사 및 가입
        memberService.signup(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.success("회원 가입 성공"));
    }
}
