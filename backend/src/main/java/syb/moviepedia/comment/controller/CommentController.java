package syb.moviepedia.comment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import syb.moviepedia.comment.dto.request.CommentSaveRequest;
import syb.moviepedia.comment.dto.request.CommentUpdateRequest;
import syb.moviepedia.comment.dto.response.CommentDetailResponse;
import syb.moviepedia.comment.dto.response.CommentEditResponse;
import syb.moviepedia.comment.dto.response.CommentListResponse;
import syb.moviepedia.comment.service.CommentService;
import syb.moviepedia.common.api.ApiSuccessResponse;
import syb.moviepedia.common.swagger.SwaggerApiResponse;
import syb.moviepedia.common.swagger.SwaggerFailResponse;
import syb.moviepedia.movie.dto.request.MovieIdRequest;

@Tag(name = "Comment API", description = "영화 코멘트 도메인 API")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/movies/{movieCode}/comments")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "상세 코멘트 조회", description = "코멘트 상세보기 화면의 정보를 조회한다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "코멘트 조회 성공"),
            @ApiResponse(
                    responseCode = "404",
                    description = "DB 코멘트 조회 실패",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerApiResponse.class)
                    )
            )
    })
    @GetMapping("/{commentId}")
    public ResponseEntity<ApiSuccessResponse<CommentDetailResponse>> getComment(
            @PathVariable Long commentId
    ) {
        return ResponseEntity.ok().body(ApiSuccessResponse.of("코멘트 조회 성공", commentService.getComment(commentId)));
    }

    @Operation(summary = "코멘트 목록 조회", description = "영화 상세페이지 코멘트 목록을 조회한다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "코멘트 목록 조회 성공"),
            @ApiResponse(
                    responseCode = "404",
                    description = "DB 조회 실패",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerApiResponse.class)
                    )
            )
    })
    @GetMapping
    public ResponseEntity<ApiSuccessResponse<CommentListResponse>> getCommentList(
            @PathVariable Long movieCode,
            Authentication authentication) {

        // 작성자 코멘트를 찾기 위한 로그인 판별. 아이디가 있으면 로그인 상태, null이면 비로그인
        // 로그인 아이디를 바탕으로 작성자 코멘트를 찾는다.
        String loinId = authentication != null ? authentication.getName() : null;

        return ResponseEntity.ok().body(ApiSuccessResponse.of("코멘트 목록 조회 성공", commentService.getAllComments(movieCode, loinId)));
    }

    @Operation(summary = "코멘트 작성", description = "코멘트를 작성하여 저장한다")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201", description = "코멘트 목록 조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerApiResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400", description = "잘못된 코멘트 값",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerFailResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404", description = "DB 조회 실패",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerApiResponse.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<ApiSuccessResponse<Void>> saveComment(
            @PathVariable Long movieCode,
            @Valid @RequestBody CommentSaveRequest dto) { // 검증은 글로벌 예외에서 처리

        commentService.saveComment(movieCode, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiSuccessResponse.of("코멘트 저장 성공"));
    }

    // 수정 화면 데이터 조회
    @Operation(summary = "수정 데이터 조회", description = "코멘트 수정 화면 데이터를 조회한다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정할 데이터 조회 성공"),
            @ApiResponse(
                    responseCode = "404", description = "DB 조회 실패",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerApiResponse.class)
                    )
            )
    })
    @GetMapping("/{commentId}/edit")
    public ResponseEntity<ApiSuccessResponse<CommentEditResponse>> getEditComment(@PathVariable Long commentId) {
        return ResponseEntity.ok().body(ApiSuccessResponse.of("코멘트 조회 성공", commentService.getEditComment(commentId)));
    }

    @Operation(summary = "코멘트 수정", description = "코멘트를 수정한다")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "수정 성공",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerApiResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400", description = "잘못된 코멘트 값",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerFailResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404", description = "코멘트 DB 조회 실패",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerApiResponse.class)
                    )
            )
    })
    @PatchMapping("/{commentId}")
    public ResponseEntity<ApiSuccessResponse<Void>> updateComment(
            @PathVariable("movieCode") Long mvCode,
            @Valid @RequestBody CommentUpdateRequest dto,
            Authentication authentication
    ) {
        String loginId = authentication.getName();
        if (dto.content() != null) {
            commentService.update(mvCode, loginId, dto);
        }
        return ResponseEntity.ok().body(ApiSuccessResponse.of(("코멘트 업데이트 성공")));
    }

    @Operation(summary = "코멘트 삭제", description = "코멘트를 삭제합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "코멘트 삭제 성공",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerApiResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "코멘트 조회 실패",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerFailResponse.class)
                    )),
            @ApiResponse(responseCode = "409", description = "코멘트 삭제 실패",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerFailResponse.class)
                    )
            )
    })
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiSuccessResponse<Void>> deleteComment(
            @PathVariable Long movieCode,
            @RequestBody MovieIdRequest movieIdRequest,
            Authentication authentication) {

        String loginId = authentication.getName();
        commentService.delete(movieCode, movieIdRequest.movieId(), loginId);
        return ResponseEntity.ok().body(ApiSuccessResponse.of("코멘트 삭제 완료"));
    }
}
