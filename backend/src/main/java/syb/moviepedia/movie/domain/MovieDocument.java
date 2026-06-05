package syb.moviepedia.movie.domain;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

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
}
