package syb.moviepedia.tv.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * TV가 아닌 TV 시리즈와 Genre를 연관관계로 묶는다
 */
@Table(name = "tv_series")
@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TVSeries {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "series_code", unique = true)
    Integer code;

    @Column(unique = true)
    String title;

    List<Integer> genres;

    List<String> countries;

    String contentRating;

    public void setContentRating(String contentRating) {
        this.contentRating = contentRating;
    }
}
