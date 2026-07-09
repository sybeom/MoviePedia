package syb.moviepedia.movie.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import syb.moviepedia.common.CreditRole;
import syb.moviepedia.common.MediaType;

/**
 * 영화와 크레딧을 연결하기 위한 중간 엔티티(테이블) 역할
 * 정확히는 약식으로 N:M관계를 표현하는 엔티티
 */
@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Credit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type")
    private MediaType mediaType;

    @Column
    private Integer code;

    @Column(name = "season_number")
    private Integer seasonNum;

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
