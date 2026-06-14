package syb.moviepedia.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum GenreType {
    ACTION(28, "액션"),
    ADVENTURE(12, "모험"),
    ANIMATION(16, "애니메이션"),
    COMEDY(35, "코미디"),
    CRIME(80, "범죄"),
    DOCUMENTARY(99, "다큐멘터리"),
    DRAMA(18, "드라마"),
    FAMILY(10751, "가족"),
    FANTASY(14, "판타지"),
    HISTORY(36, "역사"),
    HORROR(27, "공포"),
    MUSIC(10402, "음악"),
    MYSTERY(9648, "미스터리"),
    ROMANCE(10749, "로맨스"),
    SCIENCE_FICTION(878, "SF"),
    TV_MOVIE(10770, "TV 영화"),
    THRILLER(53, "스릴러"),
    WAR(10752, "전쟁"),
    WESTERN(37, "서부");

    private final int id;
    private final String name;

    public static int toId(String koreanName) {
        return Arrays.stream(values())
                .filter(genre -> genre.name.equals(koreanName))
                .findFirst()
                .map(GenreType::getId)
                .orElseThrow(() -> new IllegalArgumentException("알 수 없는 장르명: " + koreanName));
    }

    public static String getNameById(int id) {
        return Arrays.stream(values())
                .filter(genre -> genre.id == id)
                .findFirst()
                .map(genreType -> genreType.getName())
                .orElseThrow(() -> new IllegalArgumentException("알 수 없는 장르 ID: " + id));
    }
}
