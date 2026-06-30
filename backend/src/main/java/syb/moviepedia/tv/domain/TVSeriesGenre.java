package syb.moviepedia.tv.domain;

import jakarta.persistence.*;
import lombok.Builder;
import syb.moviepedia.movie.domain.Genre;

@Table(name = "tv_series_genre")
@Entity
@Builder
public class TVSeriesGenre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tv_series_id")
    private TVSeries tvSeries;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genre_id", nullable = false)
    private Genre genre;
}
