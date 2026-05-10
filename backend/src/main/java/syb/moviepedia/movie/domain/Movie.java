package syb.moviepedia.movie.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import syb.moviepedia.movie.external.tmdb.dto.TmdbInitMovie;

import java.util.List;
// TODO: DB에서 불러오는 방식이면 데이터 정보 최신화에 대해서 고민할텐데 이 부분은 일주일에 한번만 최신화를 진행하든 그렇게 해야할 듯하다.
@Builder
@Entity
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
    String releaseDate;

    List<String> country;

    String runtime;

    @Column(name = "global_rating")
    String globalRating;

    public void updateFrom(TmdbInitMovie movie) {
        this.title = movie.title();
        this.overview = movie.overview();
        this.posterPath = movie.posterPath();
        this.backdropPath = movie.backdropPath();
        this.releaseDate = movie.releaseDate();
//        this.popularity = movie.popularity();
        this.globalRating = movie.globalRating();

    }
}
