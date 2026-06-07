package syb.moviepedia.movie.domain;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "movie_search")
public class MovieDocument {
    @Id
    private String id;

    @Field(
            type = FieldType.Text,
            analyzer = "nori"
    )
    private String title;

    // 화면 표시용
    @Field(type = FieldType.Keyword, index = false)
    private String displayTitle;

    @Field(type = FieldType.Keyword)
    private String releaseYear;

    public static MovieDocument from(Movie movie) {
        String releaseYear = extractYear(movie.getReleaseDate());

        return MovieDocument.builder()
                .id(String.valueOf(movie.getCode()))
                .title(movie.getTitle())
                .displayTitle(createDisplayTitle(movie.getTitle(), releaseYear))
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
    private static String createDisplayTitle(String title, String releaseYear) {
        if (title == null || title.isBlank()) {
            return "";
        }

        if (releaseYear == null || releaseYear.isBlank()) {
            return title;
        }

        return title + " (" + releaseYear + ")";
    }
}
