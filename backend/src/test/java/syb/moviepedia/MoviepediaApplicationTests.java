package syb.moviepedia;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MoviepediaApplicationTests {

	private static final String MOVIE_TITLE_REGEX = "^(?!(?=.*\\p{L})(?!.*[가-힣]))[\\p{L}0-9 .,:~!?'\"/(){}\\[\\]&+\\-·]+$";

	@Test
	void 숫자만_있으면_통과한다() {
		String title = "12345";

		boolean result = title.matches(MOVIE_TITLE_REGEX);

		assertThat(result).isTrue();
	}

	@Test
	void 특수문자만_있으면_통과한다() {
		String title = "?!/(){}[]&+-·";

		boolean result = title.matches(MOVIE_TITLE_REGEX);

		assertThat(result).isTrue();
	}

	@Test
	void 한글이_포함되어_있으면_통과한다() {
		String title = "범죄도시 4";

		boolean result = title.matches(MOVIE_TITLE_REGEX);

		assertThat(result).isTrue();
	}

	@Test
	void 한글과_영어가_섞여도_통과한다() {
		String title = "범죄도시 THE ROUNDUP";

		boolean result = title.matches(MOVIE_TITLE_REGEX);

		assertThat(result).isTrue();
	}

	@Test
	void 영어만_있으면_실패한다() {
		String title = "Pathaan";

		boolean result = title.matches(MOVIE_TITLE_REGEX);

		assertThat(result).isFalse();
	}

	@Test
	void 일본어만_있으면_실패한다() {
		String title = "君の名は";

		boolean result = title.matches(MOVIE_TITLE_REGEX);

		assertThat(result).isFalse();
	}

	@Test
	void 중국어만_있으면_실패한다() {
		String title = "你好";

		boolean result = title.matches(MOVIE_TITLE_REGEX);

		assertThat(result).isFalse();
	}

	@Test
	void 인도문자만_있으면_실패한다() {
		String title = "Твоё";

		boolean result = title.matches(MOVIE_TITLE_REGEX);

		assertThat(result).isFalse();
	}

	@Test
	void 인도문자에_한글이_하나라도_있으면_통과한다() {
		String title = "한 अक्षय कुमार";

		boolean result = title.matches(MOVIE_TITLE_REGEX);

		assertThat(result).isTrue();
	}

	@Test
	void 허용하지_않은_특수문자가_있으면_실패한다() {
		String title = "범죄도시@";

		boolean result = title.matches(MOVIE_TITLE_REGEX);

		assertThat(result).isFalse();
	}

}
