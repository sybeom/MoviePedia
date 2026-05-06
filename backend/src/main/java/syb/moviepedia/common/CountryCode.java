package syb.moviepedia.common;

/**
 * TODO: 추후 국가 API 호출로 DB로 저장하던지 하기
 */
public enum CountryCode {
    KR("대한민국"),
    US("미국"),
    JP("일본"),
    GB("영국"),
    FR("프랑스"),
    DE("독일"),
    CN("중국"),
    CA("캐나다"),
    AU("호주"),
    ES("스페인"),
    IT("이탈리아");

    private final String koreanName;

    CountryCode(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getName() {
        return koreanName;
    }

    public static String toKoreanName(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        try {
            return CountryCode.valueOf(code).getName();
        } catch (IllegalArgumentException e) {
            return code;
        }
    }
}
