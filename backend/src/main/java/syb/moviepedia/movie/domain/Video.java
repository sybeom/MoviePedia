package syb.moviepedia.movie.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import syb.moviepedia.common.VideoType;

@Builder
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "video_key")
    @JoinColumn(name = "movie_id")
    String key;

    @Enumerated(EnumType.STRING)
    VideoType type;

    @ManyToOne(fetch = FetchType.LAZY)
    Movie movie;
}
