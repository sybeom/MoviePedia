package syb.moviepedia.movie.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import syb.moviepedia.movie.external.tmdb.dto.TmdbMovie;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
@Builder
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long code;

    private String title;

    @Column(name = "backdrop_path")
    private String backdropPath;

    @Column(name = "poster_path")
    private String posterPath;

    @Column(columnDefinition = "json")
    private List<String> genres;

    private String certification; // 관람 등급은 All, 미정 등 문자열도 있으므로 String 타입

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    private List<String> country;

    private Integer runtime;

    @Column(name = "global_rating")
    private Double globalRating;

    @Column(name = "detail_fetched")
    private Boolean detailFetched;

    public void updateFrom(TmdbMovie movie, String certification) {
        this.title = movie.title();
        this.overview = movie.overview();
        this.posterPath = movie.posterPath();
        this.backdropPath = movie.backdropPath();
        this.certification = certification;
        this.releaseDate = movie.releaseDate();
        this.globalRating = movie.globalRating();
    }

    // 상세 정보 업데이트
    public void update(String certification, List<String> country, Integer runtime) {
        this.certification = certification;
        this.country = country;
        this.runtime = runtime;
        detailFetched = true;
    }
}
