package syb.moviepedia.movie.domain;

import jakarta.persistence.*;
import lombok.*;
import syb.moviepedia.common.MediaCategoryType;
import syb.moviepedia.media.BaseMediaCategoryEntity;

/**
 * 홈화면 인기, 현재 상영, 개봉 예정 영화 엔티티
 */
@Entity
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MovieCategory extends BaseMediaCategoryEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @Builder
    MovieCategory(
            MediaCategoryType type,
            Movie movie
    ) {
        super(type);
        this.movie = movie;
    }
}
