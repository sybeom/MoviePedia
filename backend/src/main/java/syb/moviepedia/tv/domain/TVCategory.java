package syb.moviepedia.tv.domain;

import jakarta.persistence.*;
import lombok.Builder;
import syb.moviepedia.common.MovieCategoryType;
import syb.moviepedia.media.BaseMediaCategoryEntity;

@Table(name = "tv_category")
@Entity
public class TVCategory extends BaseMediaCategoryEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tv_id")
    private TV tv;

    @Builder
    public TVCategory(
            MovieCategoryType movieCategoryType,
            TV tv
    ) {
        super(movieCategoryType);
        this.tv = tv;
    }
}
