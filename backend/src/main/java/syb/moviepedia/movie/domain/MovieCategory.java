package syb.moviepedia.movie.domain;

import jakarta.persistence.*;
import lombok.*;
import syb.moviepedia.common.MovieCategoryType;

import java.time.LocalDateTime;

/**
 * 홈화면 인기, 현재 상영, 개봉 예정 영화 엔티티
 */
@Builder
@Entity
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MovieCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private MovieCategoryType categoryType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "code")
    private Movie movie;

    private Double popularity;

    private LocalDateTime fetchedAt;
}
