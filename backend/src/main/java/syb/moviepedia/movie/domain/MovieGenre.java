package syb.moviepedia.movie.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 영화-장르의 N:M 관계를 표현하기 위한 중간 테이블(엔티티)
 * 굳이 별도로 MovieGenre 클래스를 둔 이유는, 홈 화면 장르 필터에서 필요하기 때문이다.
 */
@Entity
@NoArgsConstructor
@Getter
public class MovieGenre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 영화
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    // 장르
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genre_id", nullable = false)
    private Genre genre;

    @Builder
    public MovieGenre(Movie movie, Genre genre) {
        this.movie = movie;
        this.genre = genre;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }
}
