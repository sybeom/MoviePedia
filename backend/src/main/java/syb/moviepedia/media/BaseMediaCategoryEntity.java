package syb.moviepedia.media;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import syb.moviepedia.common.MovieCategoryType;

@MappedSuperclass
public abstract class BaseMediaCategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private MovieCategoryType categoryType;

    protected BaseMediaCategoryEntity(
            MovieCategoryType categoryType
    ) {
        this.categoryType = categoryType;
    }
}
