package syb.moviepedia.movie.domain;

import jakarta.persistence.*;
import syb.moviepedia.common.MovieCategoryType;

import java.time.LocalDateTime;

/**
 * 홈화면 인기, 현재 상영, 개봉 예정 영화 목록 엔티티
 */
@Entity
public class MovieCategory {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private MovieCategoryType categoryType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    private Movie movie;

    private Integer ranking;

    private LocalDateTime fetchedAt;
}
