package syb.moviepedia.elasticsearch.domain;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
