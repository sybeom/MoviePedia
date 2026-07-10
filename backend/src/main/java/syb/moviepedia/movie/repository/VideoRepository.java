package syb.moviepedia.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import syb.moviepedia.common.MediaType;
import syb.moviepedia.movie.domain.Movie;
import syb.moviepedia.movie.domain.Video;

import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long> {

    boolean existsByMediaTypeAndCode(MediaType mediaType, Integer movieCode);

    // 예고편 -> 티저 순으로 정렬. 예고 및 티저는 그중에서도 출시일으로 정렬한다.
    @Query("""
        select v
        from Video v
        where v.code=:movieCode and v.mediaType=:mediaType
        order by 
            case
                when v.videoType = syb.moviepedia.common.VideoType.TRAILER then 0
                when v.videoType = syb.moviepedia.common.VideoType.TEASER then 1
                else 2
            end,
            v.publishedAt asc
    """)
    List<Video> findByVideo(@Param("mediaType")MediaType mediaType,@Param("movieCode") Integer code);

}
