package syb.moviepedia.media;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.util.List;

/**
 * 영화와 TV 시리즈에 들어가는 공통된 컬럼을 모아둔 클래스
 *
 */
@MappedSuperclass
public abstract class BaseMediaEntity {
    private Long code;

    private String title;

    @Column(name = "poster_path")
    private String posterPath;

    private String certification; // 관람 등급은 All, 미정 등 문자열도 있으므로 String 타입

    private String overview;

    @Column(name = "release_date")
    private String releaseDate;

    private List<String> country;

    @Column(name = "detail_fetched")
    private Boolean detailFetched;

    @Column(name="comment_count",nullable = false)
    private long commentCount=0;

    @Column(name = "like_count", nullable = false)
    private long likeCount=0;
}
