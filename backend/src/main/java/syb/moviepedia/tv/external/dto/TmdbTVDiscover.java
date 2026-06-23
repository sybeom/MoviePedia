package syb.moviepedia.tv.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Tmdb TV discover api 호출
 * 해당 api는 TV 시리즈 목록들을 불러온다.
 */
public record TmdbTVDiscover(
        List<TmdbTVResult> results
) {
    public record TmdbTVResult(
            Integer id
    ) { }
}
