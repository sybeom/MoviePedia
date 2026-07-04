package syb.moviepedia.tv.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import syb.moviepedia.media.BaseMediaEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * TV가 아닌 TV 시리즈와 Genre를 연관관계로 묶는다
 */
@Table(name = "tv_series")
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverrides({
        @AttributeOverride(
                name = "code",
                column = @Column(name = "series_code", unique = true)
        )
})
public class TVSeries extends BaseMediaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Builder
    public TVSeries(
            Integer code,
            String title,
            String certification,
            List<String> country,
            String overview,
            String posterPath,
            String backdropPath) {
        super(code, title, certification, country, overview, posterPath, backdropPath);

    }
}
