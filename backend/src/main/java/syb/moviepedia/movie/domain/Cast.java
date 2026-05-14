package syb.moviepedia.movie.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * 영화와 출연진(캐스팅)을 연결하가 위한 중간 엔티티(테이블)
 */
@Table(name = "movie_cast")
@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cast {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    Movie movie;

    Long actorId;

    String name;

    String profile;

    @Column(name = "cast_order")
    Integer castOrder;
}
