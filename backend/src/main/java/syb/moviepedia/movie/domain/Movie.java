package syb.moviepedia.movie.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import syb.moviepedia.movie.external.tmdb.dto.TmdbMovie;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
@Builder
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long code;

    private String title;

    @Column(name = "backdrop_path")
    private String backdropPath;

    @Column(name = "poster_path")
    private String posterPath;

    @Column(columnDefinition = "json")
    private List<String> genres;

    private String certification; // 관람 등급은 All, 미정 등 문자열도 있으므로 String 타입

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    private List<String> country;

    private Integer runtime;

    @Column(name = "global_rating")
    private Double globalRating;

    @Column(name = "detail_fetched")
    private Boolean detailFetched;

    @Builder.Default
    @Column(name = "rating_sum")
    private Double ratingSum = 0.0;      // 평점 총합

    @Builder.Default
    @Column(name = "rating_count")
    private Long ratingCount = 0L;      // 평점 개수

    @Builder.Default
    @Column(name = "rating")
    private Double averageRating = 0.0;  // 평균 평점

    public void updateFrom(TmdbMovie movie, String certification) {
        this.title = movie.title();
        this.overview = movie.overview();
        this.posterPath = movie.posterPath();
        this.backdropPath = movie.backdropPath();
        this.certification = certification;
        this.releaseDate = movie.releaseDate();
        this.globalRating = movie.globalRating();
    }

    // 상세 정보 업데이트
    public void update(List<String> country, Integer runtime) {
        this.country = country;
        this.runtime = runtime;
        detailFetched = true;
    }

    // 코멘트 작성시 평점 저장
    public void saveRating(double newRating) {
        this.ratingSum += newRating;
        this.ratingCount++;
        this.averageRating = roundToOneDecimal(this.ratingSum / this.ratingCount);
    }

    // 코멘트 평점 수정
    public void updateRating(double oldRating, double newRating) {
        this.ratingSum = this.ratingSum - oldRating + newRating;
        this.averageRating = roundToOneDecimal(this.ratingSum / this.ratingCount);
    }

    // 코멘트 삭제 평점 수정
    public void removeRating(double rating) {
        this.ratingSum -= rating;
        this.ratingCount--;

        if (this.ratingCount <= 0) {
            this.ratingSum = 0.0;
            this.ratingCount = 0L;
            this.averageRating = null;
            return;
        }

        this.averageRating = roundToOneDecimal(this.ratingSum / this.ratingCount);
    }

    public Double getDisplayRating() {
        if (this.ratingCount < 20) {
            return null;
        }
        return this.averageRating;
    }

    // 소수점 둘째자리에서 반올림 (첫째자리까지 표현)
    private double roundToOneDecimal(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
