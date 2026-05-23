package syb.moviepedia.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import syb.moviepedia.comment.domain.Comment;
import syb.moviepedia.member.domain.Member;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {


    Optional<Comment> findByMovieId(Long mvId);
    Boolean existsByMovieIdAndMemberId(Long  mvId, Long memberId);

    //내가 작성한 코멘트 조회
    @Query("""
        select c
        from Comment c
        where c.movie.id = :movieId
            and c.member.loginId=:loginId     
    """)
    Optional<Comment> findByMovieIdAndLoginId(@Param("movieId") Long mvId, @Param("loginId") String loginId);

    // 코멘트 작성자 찾기
    @Query("""
        select c.member
        from Comment c
        where c.id=:commentId
    """)
    Optional<Member> findByCommentId(@Param("commentId")Long id);

    // 코멘트 목록에서 로그인한 유저가 작성한 코멘트가 있으면 해당 코멘트를 가장 앞에 정렬하여 코멘트 목록을 반환하는 쿼리
    @Query("""
        select c
        from Comment c
        where c.movie.id = :movieId
        order by 
            case when c.member.loginId = :loginId then 0 else 1 end
    """)
    List<Comment> findByMovieIdWithMyCommentFirst(
            @Param("movieId") Long mvId,
            @Param("loginId") String loginId);

    // 영화에 달린 코멘트 개수
    @Query("""
        select count(c)
        from Comment c
        where c.movie.id=:movieId
    """)
    Long findCommentsCountByMovieId(@Param("movieId") Long mvId);

    // 영화 평점 계산
    @Query("""
        select round(avg(c.rating),1)
        from Comment c
        where c.movie.id=:movieId
    """)
    Double findCommentsRatingAverage(@Param("movieId") Long mvId);
}
