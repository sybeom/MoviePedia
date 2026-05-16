package syb.moviepedia.movie.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import syb.moviepedia.common.CreditRole;

/**
 * 영화와 크레딧을 연결하기 위한 중간 엔티티(테이블) 역할
 * 정확히는 약식으로 N:M관계를 표현하는 엔티티
 */
@Table(name = "movie_cast")
@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Credit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 영화의 크레딧인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    // 이름
    @Column(nullable = false)
    private String name;

    // 프로필 이미지
    private String profile;

    // 배우인지 감독인지
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CreditRole role;

    // 배우 순서
    @Column(name = "cast_order")
    private Integer castOrder;
}
