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

    @Column(name = "movie_id", unique = true, nullable = false)
    Long movieId;

    String title;

    @Column(name = "backdrop_path")
    String backdropPath;

    @Column(name = "poster_path")
    String posterPath;

    @Column(columnDefinition = "json")
    List<String> genres;

    String certification; // 관람 등급은 All, 미정 등 문자열도 있으므로 String 타입

    @Column(columnDefinition = "TEXT")
    String overview;

    @Column(name = "release_date")
    LocalDate releaseDate;

    List<String> country;

    Integer runtime;

    @Column(name = "global_rating")
    Double globalRating;

    @Column(name = "detail_fetched")
    Boolean detailFetched;

    public void updateFrom(TmdbMovie movie, String certification) {
        this.title = movie.title();
        this.overview = movie.overview();
        this.posterPath = movie.posterPath();
        this.backdropPath = movie.backdropPath();
        this.certification = certification;
        this.releaseDate = movie.releaseDate();
        this.globalRating = Math.round(movie.globalRating() * 10) / 10.0; // 소수점 둘째자리에서 반올림
    }

    public void update(String certification, List<String> country, Integer runtime) {
        this.certification = certification;
        this.country = country;
        this.runtime = runtime;
        detailFetched = true;
    }
}
