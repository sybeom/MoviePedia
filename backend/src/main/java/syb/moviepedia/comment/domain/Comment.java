package syb.moviepedia.comment.domain;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNullApi;
import syb.moviepedia.member.domain.Member;
import syb.moviepedia.movie.domain.Movie;

// TODO: 모든 엔티티 접근자 private으로 변경하기
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String nickname;

    @Nullable
    String content;

    @Nullable
    Double rating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    Member member;
}
