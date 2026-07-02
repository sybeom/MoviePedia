package syb.moviepedia.media;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    private List<String> country;

    @Column(name = "detail_fetched")
    private Boolean detailFetched;

    @Column(name="comment_count",nullable = false)
    private long commentCount=0;

    @Column(name = "like_count", nullable = false)
    private long likeCount=0;

    @Column(name = "poster_path")
    private String posterPath;

    protected BaseMediaEntity(
            Integer code,
            String title,
            String posterPath,
            String certification,
            String overview,
            LocalDate releaseDate,
            List<String> country,
            Boolean detailFetched
    ) {
        this.code = code;
        this.title = title;
        this.posterPath = posterPath;
        this.certification = certification;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.country = country;
        this.detailFetched = detailFetched;
    }

    protected void updateAirDate(LocalDate airDate) {
        this.releaseDate = airDate;
    }

    protected void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }
}
