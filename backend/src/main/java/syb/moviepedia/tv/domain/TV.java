package syb.moviepedia.tv.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import syb.moviepedia.common.ReactionType;

import java.time.LocalDate;


/**
 * TV 시즌별 엔티티
 */
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TV {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "series_code")
    private Integer seriesCode;

    @Column(name = "season_number")
    private Integer seasonNum;

    @Column(name = "episode_count")
    private Integer episodeCnt;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column(name = "poster_path")
    private String posterPath;

    @Column(name="comment_count",nullable = false)
    private long commentCount=0;

    @Column(name = "like_count", nullable = false)
    private long likeCount=0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id")
    private TVSeries series;

    // 코멘트 개수 및 좋아요 수 상태 업데이트
    public void increaseCommentStats(ReactionType reactionType) {
        this.commentCount++;

        if (reactionType==ReactionType.LIKE)
            this.likeCount++;
    }

    public void decreaseCommentStats() {
        this.commentCount--;
        this.likeCount--;
    }
}
