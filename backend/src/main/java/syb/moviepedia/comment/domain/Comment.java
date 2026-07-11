package syb.moviepedia.comment.domain;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import syb.moviepedia.comment.dto.request.CommentUpdateRequest;
import syb.moviepedia.common.MediaType;
import syb.moviepedia.common.ReactionType;
import syb.moviepedia.member.domain.Member;
import syb.moviepedia.movie.domain.Movie;
import syb.moviepedia.tv.domain.TV;

import java.time.LocalDateTime;

@Slf4j
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Entity
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickname;

    @Nullable
    @Column(length = 300)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false)
    private MediaType mediaType;

    @Column(name = "media_code")
    private Integer code;

    @Column(name = "season_number")
    private Integer seasonNum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tv_id")
    private TV tv;

    @Enumerated(EnumType.STRING)
    private ReactionType reactionType;

    @CreatedDate
    @Column(name = "created_date_at", updatable = false)
    private LocalDateTime createdDateAt;

    // 코멘트 수정
    public void update(CommentUpdateRequest dto) {
        this.content = dto.content();
    }
}
