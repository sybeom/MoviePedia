package syb.moviepedia.tv.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import syb.moviepedia.common.MediaCategoryType;
import syb.moviepedia.media.BaseMediaCategoryEntity;

@Table(name = "tv_category")
@Getter
@Entity
@NoArgsConstructor
public class TVCategory extends BaseMediaCategoryEntity {
    private Integer code;

    private String title;

    @Column(name = "backdrop_path")
    private String backdropPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tv_id")
    private TV tv;



    @Builder
    public TVCategory(
            Integer code,
            String title,
            String backdropPath,
            MediaCategoryType mediaCategoryType,
            TV tv
    ) {
        super(mediaCategoryType);
        this.code = code;
        this.title = title;
        this.backdropPath = backdropPath;
        this.tv = tv;
    }
}
