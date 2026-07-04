package syb.moviepedia.tv.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


/**
 * TV 시즌별 엔티티
 */
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TV {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "series_code")
    private Integer seriesCode;

    @Column(name = "season_number")
    private Integer seasonNum;

    @Column(name = "episode_count")
    private Integer episodeCnt;

    @Column(name = "release_date")
    LocalDate releaseDate;

    @Column(name = "poster_path")
    String posterPath;

    @Column(name="comment_count",nullable = false)
    private long commentCount=0;

    @Column(name = "like_count", nullable = false)
    private long likeCount=0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id")
    TVSeries series;

    public void setSeries(TVSeries series) {
        this.series = series;
    }
}
