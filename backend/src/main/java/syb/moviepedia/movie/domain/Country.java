package syb.moviepedia.movie.domain;

import jakarta.persistence.*;
import lombok.Builder;

@Entity
@Builder
public class Country {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true)
    String code;
    @Column(unique = true)
    String name;
}
