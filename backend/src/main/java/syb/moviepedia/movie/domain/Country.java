package syb.moviepedia.movie.domain;

import jakarta.persistence.*;
import lombok.Builder;

@Entity
@Builder
public class Country {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String code;
    @Column(unique = true)
    private String name;
}
