package syb.moviepedia.tv.domain;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import syb.moviepedia.media.BaseMediaEntity;
import syb.moviepedia.tv.service.TVService;

import java.time.LocalDate;
import java.util.List;


/**
 * TV 시즌별 엔티티
 */
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
@Entity
@Getter
@NoArgsConstructor
public class TV extends BaseMediaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "season_number")
    private Integer seasonNum;

    @Column(name = "episode_count")
    private Integer episodeCnt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id")
    TVSeries series;

    @Builder
    public TV(
            Integer code,
            String title,
            Integer seasonNum,
            Integer episodeCnt,
            String posterPath,
            String contentRating,
            String overview,
            LocalDate releaseDate,
            List<String> country,
            Boolean detailFetched

    ) {
        super(code, title, posterPath, contentRating, overview, releaseDate, country, detailFetched);
        this.seasonNum = seasonNum;
        this.episodeCnt = episodeCnt;
    }

    public void setSeries(TVSeries series) {
        this.series = series;
    }
}
