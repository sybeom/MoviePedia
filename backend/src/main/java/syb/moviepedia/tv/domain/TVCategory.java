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
    @Column(name = "series_code")
    private Integer seriesCode;

    @Column(name = "season_number", nullable = false)
    private Integer seasonNumber;

    private String title;


    @Column(name = "backdrop_path")
    private String backdropPath;



    @Builder
    public TVCategory(
            Integer seriesCode,
            Integer seasonNumber,
            String title,
            String backdropPath,
            MediaCategoryType mediaCategoryType
    ) {
        super(mediaCategoryType);
        this.seriesCode = seriesCode;
        this.seasonNumber = seasonNumber;
        this.title = title;
        this.backdropPath = backdropPath;
    }
}
