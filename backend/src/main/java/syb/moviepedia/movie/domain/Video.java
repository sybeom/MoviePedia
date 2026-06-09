package syb.moviepedia.movie.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import syb.moviepedia.common.VideoType;

import java.time.Instant;

@Builder
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "video_key", nullable = false, unique = true)
    @JoinColumn(name = "movie_id")
    private String key;

    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private VideoType type;

    @Column(name = "published_at")
    private Instant publishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    Movie movie;
}
