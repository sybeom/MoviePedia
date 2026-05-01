package syb.moviepedia.common.exception;

public class JsonParsingFailedException extends RuntimeException {
    public JsonParsingFailedException(String message) {
        super(message);
    }
}
