package syb.moviepedia.tv.domain;


import jakarta.persistence.*;
import syb.moviepedia.media.BaseMediaEntity;

@Entity
@AttributeOverrides({
        @AttributeOverride(
                name = "code",
                column = @Column(name = "tv_code")
        ),
})
public class TV extends BaseMediaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "season_number")
    Integer seasonNum;


}
