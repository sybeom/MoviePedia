package syb.moviepedia.media;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import syb.moviepedia.common.MediaCategoryType;

@NoArgsConstructor
@MappedSuperclass
public abstract class BaseMediaCategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private MediaCategoryType categoryType;

    protected BaseMediaCategoryEntity(
            MediaCategoryType categoryType
    ) {
        this.categoryType = categoryType;
    }
}
