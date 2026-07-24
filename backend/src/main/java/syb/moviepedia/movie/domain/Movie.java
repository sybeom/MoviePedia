package syb.moviepedia.movie.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import syb.moviepedia.common.ReactionType;
import syb.moviepedia.media.BaseMediaEntity;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverrides({
        @AttributeOverride(
                name = "code",
                column = @Column(name = "movie_code",unique = true, nullable = false)
        ),
        @AttributeOverride(
                name = "backdropPath",
                column = @Column(name = "backdrop_path")
        ),
        @AttributeOverride(
                name = "posterPath",
                column = @Column(name = "poster_path")
        )
})
public class Movie extends BaseMediaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "release_date")
    LocalDate releaseDate;

    Integer runtime;

    @Column(name="comment_count",nullable = false)
    private long commentCount=0;

    @Column(name = "like_count", nullable = false)
    private long likeCount=0;

    @Column(name = "detail_fetched", nullable = false)
    private boolean detailFetched = false;

    @Builder
    public Movie(
            Integer code,
            String title,
            String certification,
            LocalDate releaseDate,
            List<String> country,
            String posterPath,
            String backdropPath,
            String overview,
            Integer runtime,
            long commentCount,
            long likeCount) {
        super(code, title, certification, country, overview, posterPath);
        this.releaseDate = releaseDate;
        this.commentCount = commentCount;
        this.likeCount = likeCount;
        this.runtime = runtime;
        this.detailFetched = false;
    }

    // 상세 정보 업데이트
    public void updateCountryAndRuntime(List<String> country, Integer runtime) {
        super.updateCountryAndRuntime(country);
    }

    // 코멘트 개수 및 좋아요 수 상태 업데이트
    public void increaseCommentStats(ReactionType reactionType) {
        this.commentCount++;
        if (reactionType==ReactionType.LIKE)
            this.likeCount++;
    }

    // 코멘트 개수 및 좋아요 수 상태 업데이트
    public void decreaseCommentStats() {
        this.commentCount--;
        this.likeCount--;
    }

    public int getLikeRate() {
        if (commentCount == 0) {
            return 0;
        }
        return (int) Math.round((double) likeCount / commentCount * 100);
    }

    public void setDetail(
            String title,
            LocalDate releaseDate,
            String certification,
            Integer runtime,
            List<String> countries,
            String overview,
            String posterPath,
            String backdropPath) {
        super.updateMovieDetail(title, certification, countries, overview, posterPath, backdropPath);
        this.releaseDate = releaseDate;
        this.runtime = runtime;
        this.detailFetched = true;
    }
}
