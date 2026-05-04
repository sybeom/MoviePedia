package syb.moviepedia.common.swagger;

import io.swagger.v3.oas.annotations.media.Schema;
import syb.moviepedia.movie.dto.TmdbMovieSummaryDto;

import java.util.List;

/**
 * 홈(메인) 화면 영화 목록 Swagger 응답 명세 클래스
 */
@Schema(name = "TmdbMovieSummaryListResponse")
public record SwaggerTmdbMovieSummaryListResponse(
        @Schema(example = "SUCCESS")
        String code,

        @Schema(example = "영화 목록 조회 성공")
        String message,

        List<TmdbMovieSummaryDto> data
) {
}
