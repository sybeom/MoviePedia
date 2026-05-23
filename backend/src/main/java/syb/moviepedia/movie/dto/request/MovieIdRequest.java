package syb.moviepedia.movie.dto.request;

/**
 * 영화 id를 전달 받기 위한 요청 DTO
 * 굳이 영화 Id를 보내는 이유는 코멘트를 삭제하기 위해선 영화 id가 필요한데,
 * 영화 Id를 얻기 위해 영화 코드 같은 것으로 추가적으로 쿼리를 실행하여
 * id를 얻는 것을 줄이기 위해서이다
 */
public record MovieIdRequest(
        Long movieId
) {
}
