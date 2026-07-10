package syb.moviepedia.movie.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import syb.moviepedia.common.MediaType;
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

    @Column(nullable = false)
    private Integer code;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false)
    MediaType mediaType;

    @Column(name = "video_key", nullable = false, unique = true)
    @JoinColumn(name = "movie_id")
    private String key;

    @Column(name = "video_type", nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private VideoType videoType;

    @Column(name = "published_at")
    private Instant publishedAt;

}
