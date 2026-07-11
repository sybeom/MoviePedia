package syb.moviepedia.media;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import syb.moviepedia.movie.domain.Country;

import java.time.LocalDate;
import java.util.List;

/**
 * 영화와 TV 시리즈에 들어가는 공통된 컬럼을 모아둔 클래스
 *
 */
@MappedSuperclass
@NoArgsConstructor
@Getter
public abstract class BaseMediaEntity {
    private Integer code;

    private String title;

    private String certification; // 관람 등급은 All, 미정 등 문자열도 있으므로 String 타입

    private List<String> country;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column(name = "poster_path")
    private String posterPath;

    @Column(name = "backdrop_path")
    private String backdropPath;

    protected BaseMediaEntity(
            Integer code,
            String title,
            String certification,
            List<String> country,
            String overview,
            String posterPath
    ) {
        this.code = code;
        this.title = title;
        this.certification = certification;
        this.country = country;
        this.overview = overview;
        this.posterPath = posterPath;
    }

    public BaseMediaEntity(
            Integer code,
            String title,
            String certification,
            List<String> country,
            String overview,
            String posterPath,
            String backdropPath) {
        this.code = code;
        this.title = title;
        this.certification = certification;
        this.overview = overview;
        this.country = country;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
    }

    protected void updateCountryAndRuntime(List<String> country) {
        this.country = country;
    }

    public void updateOverviewAndPosterPath(String overview, String posterPath, String backdropPath) {
        this.overview = overview;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
    }

    protected void setCountries(List<String> countries) {
        this.country = countries;
    }
}
