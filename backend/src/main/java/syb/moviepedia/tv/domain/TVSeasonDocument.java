package syb.moviepedia.tv.domain;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDate;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setting(settingPath = "elasticsearch/media-settings.json")
@Document(indexName = "tv_season_search")
public class TVSeasonDocument {
    @Id
    private String id;

    @MultiField(
            mainField = @Field(
                    type = FieldType.Text,
                    analyzer = "nori"
            ),
            otherFields = {
                    @InnerField(
                            suffix = "space_ngram",
                            type = FieldType.Text,
                            analyzer = "ngram_index_analyzer",
                            searchAnalyzer = "nori"
                    ),
                    @InnerField(
                            suffix ="no_space_ngram",
                            type = FieldType.Text,
                            analyzer = "no_space_ngram_analyzer",
                            searchAnalyzer = "no_space_ngram_search_analyzer"
                    ),
                    @InnerField(
                            suffix = "keyword",
                            type = FieldType.Keyword
                    )
            }
    )
    private String title;

    // 화면 표시용
    @Field(type = FieldType.Keyword, index = false)
    private String displayTitle;

    @Field(type = FieldType.Keyword)
    private String releaseYear;

    @Field(type = FieldType.Keyword)
    private String seriesCode;

    @Field(type = FieldType.Keyword)
    private String seasonNumber;


    public static TVSeasonDocument from(TV tv) {
        String releaseYear = extractYear(tv.getReleaseDate());

        return TVSeasonDocument.builder()
                .seriesCode(String.valueOf(tv.getSeriesCode()))
                .seasonNumber(String.valueOf(tv.getSeasonNum()))
                .title(tv.getSeries().getTitle())
                .displayTitle(createDisplayTitle(tv.getSeries().getTitle(), tv.getSeasonNum(), releaseYear))
                .releaseYear(releaseYear)
                .build();
    }

    // 연도 추출
    private static String extractYear(LocalDate releaseDate) {
        if (releaseDate == null) {
            return null;
        }

        return String.valueOf(releaseDate.getYear());
    }

    // 같은 제목의 영화가 있으므로 연도로 구분.
    // 형식: 영화 (연도)
    private static String createDisplayTitle(String title,Integer seasonNum, String releaseYear) {
        if (title == null || title.isBlank()) {
            return "";
        }

        if (releaseYear == null || releaseYear.isBlank()) {
            return title;
        }

        return title + " 시즌" + seasonNum + " (" + releaseYear + ")";
    }
}
