package syb.moviepedia.common.util;

public final class CommonUtils {
    private CommonUtils() {} // 객체 생성 방지

    private static final String TITLE_PATTERN = "^(?!(?=.*\\p{L})(?!.*[가-힣]))[\\p{L}0-9 .,:~!?'\"/(){}\\[\\]&+\\-·]+$";

    public static String mapTvRating(String rating) {
        if (rating == null || rating.isBlank()) {
            return null;
        }

        return switch (rating) {
            case "TV-Y", "TV-Y7", "TV-G" -> "ALL";
            case "TV-PG" -> "12";
            case "TV-14" -> "15";
            case "TV-MA" -> "19";
            case "NR" -> null;
            default -> null;
        };
    }

    public static boolean isTitleMatch(String title) {
        return title.matches(TITLE_PATTERN);
    }
}
