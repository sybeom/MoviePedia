package syb.moviepedia.comment.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import syb.moviepedia.comment.domain.Comment;
import syb.moviepedia.common.ReactionType;
import syb.moviepedia.member.domain.Member;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

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

    // 코멘트 목록
    @Query("""
        select c
            from Comment c
            join c.movie movie
            join fetch c.member member
            where movie.code = :movieCode
    """)
    Slice<Comment> findByCommentsMovieId(@Param("movieCode") Long mvCode, Pageable pageable);

    // 영화에 달린 코멘트 개수
    @Query("""
        select count(c)
        from Comment c
        where c.movie.id=:movieId
    """)
    Long findCommentsCountByMovieId(@Param("movieId") Long mvId);

    // 코멘트 조회시 영화도 함께 가져오기
    @Query("""
        select c
        from Comment c
        join fetch c.movie
        where c.movie.id = :movieId
          and c.movie.code = :mvCode
          and c.member.loginId = :loginId
    """)
    Optional<Comment> findMyCommentWithMovie(
            @Param("mvCode") Long mvCode,
            @Param("movieId") Long movieId,
            @Param("loginId") String loginId
    );
}
