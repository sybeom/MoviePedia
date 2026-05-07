package syb.moviepedia.movie.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Builder;
import syb.moviepedia.movie.external.tmdb.dto.TmdbGenre;

import java.util.List;
// TODO: DB에서 불러오는 방식이면 데이터 정보 최신화에 대해서 고민할텐데 이 부분은 일주일에 한번만 최신화를 진행하든 그렇게 해야할 듯하다.
@Builder
@Entity
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

    List<TmdbGenre> genres;

    String overview;

    @Column(name = "release_date")
    String releaseDate;

    List<String> country;

    String runtime;

    @Column(name = "global_rating")
    String globalRating;

    String certification;
}
