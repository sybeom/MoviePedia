package syb.moviepedia.tv.domain;


import jakarta.persistence.*;
import lombok.Builder;
import syb.moviepedia.media.BaseMediaEntity;

import java.util.List;


/**
 * TV 시즌별 엔티티
 */
@Entity
@AttributeOverrides({
        @AttributeOverride(
                name = "code",
                column = @Column(name = "series_code")
        ),
        @AttributeOverride(
                name = "title",
                column = @Column(name = "series_title")
        )
})
public class TV extends BaseMediaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "season_number")
    private Integer seasonNum;

    @Column(name = "episode_count")
    private Integer episodeCnt;

    @Builder
    public TV(
            Integer code,
            String title,
            Integer seasonNum,
            Integer episodeCnt,
            String posterPath,
            String certification,
            String overview,
            String releaseDate,
            List<String> country,
            Boolean detailFetched

    ) {
        super(code, title, posterPath, certification, overview, releaseDate, country, detailFetched);
        this.seasonNum = seasonNum;
        this.episodeCnt = episodeCnt;
    }
}
