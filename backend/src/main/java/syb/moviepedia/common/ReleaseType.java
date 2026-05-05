package syb.moviepedia.common;

/**
 * TMDB 개봉일 api에서 type 의미를 정의하는 enum 클래스
 */
public enum ReleaseType {
    PREMIERE(1), // 1, 최초 공개 시사회 / 영화제 상영
    THEATRICAL(3), // 3, 일반 극장 개봉
    DIGITAL(4), // 4,  디지털 공개(Disney+, Netflix 등 OTT 및 VOD, 또는 스트리밍)
    PHYSICAL(5), // 5, 물리 매체 출시(DVD, 블루 레이 등)
    TV(6); // 6, TV 방영

    private int releaseType;
    ReleaseType(final int releaseType) {
        this.releaseType = releaseType;
    }

    public int getType() {
        return releaseType;
    }

    public boolean matches(Integer type) {
        return type != null && type == this.releaseType;
    }
}
