package syb.moviepedia.tv.domain;

import jakarta.persistence.*;
import lombok.Builder;

import java.util.List;

/**
 * TV가 아닌 TV 시리즈와 Genre를 연관관계로 묶는다
 */
@Table(name = "tv_series")
@Entity
@Builder
public class TVSeries {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "series_code")
    Integer code;

    String title;

    List<Integer> genres;

    List<String> countries;

    String contentRating;
}
