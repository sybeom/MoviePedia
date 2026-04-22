package syb.moviepedia.jwt.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * jwt CRUD를 위한 엔티티
 */
@Entity
@EntityListeners(AuditingEntityListener.class) // 엔티티 생성시 생성시간이 부여됨
@Table(name = "jwt_refresh")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtRefresh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false)
    private String loginId;

    @Column(name = "refresh", nullable = false, length = 512)
    private String refresh; // 리프레쉬 토큰 저장

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate; // 리프레쉬 토큰 발급 시간
}
