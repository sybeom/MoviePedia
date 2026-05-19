package syb.moviepedia.comment.domain;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNullApi;
import syb.moviepedia.member.domain.Member;
import syb.moviepedia.movie.domain.Movie;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickname;

    @Nullable
    private String content;

    @Nullable
    private Double rating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder.Default // Builder로 객체 생성시 like 값을 지정하지 않아도 기본값이 들어간다.
    @Column(name = "like_count", nullable = false)
    Integer like = 0;// 시작값 0 고정

    public void updateContent(String content) {
        this.content = content;
    }
}
