package syb.moviepedia.like.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import syb.moviepedia.comment.domain.Comment;
import syb.moviepedia.member.domain.Member;

@Table(
        name = "likes",
        // 같은 사용자가 같은 코멘트에 중복 좋아요 방지
        uniqueConstraints = @UniqueConstraint(columnNames = {"comment_id", "member_id"})
)
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;
}
