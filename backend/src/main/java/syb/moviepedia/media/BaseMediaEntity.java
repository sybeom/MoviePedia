package syb.moviepedia.media;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.util.List;

@MappedSuperclass
public abstract class BaseMediaEntity {
    private String title;
    private String backdropPath;
    private String posterPath;
    private String certification; // 관람 등급은 All, 미정 등 문자열도 있으므로 String 타입
    private Long code;
    private String overview;
    private String releaseDate;
    private List<String> country;
    private Boolean detailFetched;
    private long commentCount=0;
    private long likeCount=0;
}
