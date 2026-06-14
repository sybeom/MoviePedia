package syb.moviepedia.movie.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

/**
 * 영화-장르의 N:M 관계를 표현하기 위한 중간 테이블(엔티티)
 */
@Entity
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
