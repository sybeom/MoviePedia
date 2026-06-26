package syb.moviepedia.tv.domain;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import syb.moviepedia.media.BaseMediaEntity;

import java.util.List;

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

    @Builder
    public TV(
            Integer code,
            String title,
            String posterPath,
            String certification,
            String overview,
            String releaseDate,
            List<String> country,
            Boolean detailFetched,
            Integer seasonNum
    ) {
        super(code, title, posterPath, certification, overview, releaseDate, country, detailFetched);
        this.seasonNum = seasonNum;
    }

}
