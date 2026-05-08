package syb.moviepedia.movie.domain;

import jakarta.persistence.*;
import jdk.jfr.Contextual;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 장르 정보 엔티티
 */
@Entity
@Builder
@AllArgsConstructor
@Table(
        uniqueConstraints = { // 유니크 제약 (중복 저장 방지)
                @UniqueConstraint(columnNames = "tmdb_genre_id")
        }
)
public class TmdbGenre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "genre_id", unique = true, nullable = false)
    private Integer genreId;

    @Column(nullable = false)
    private String name;
}
