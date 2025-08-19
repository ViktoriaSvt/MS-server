package skytales.Library.util.exceptions;

public class BookSearchException extends RuntimeException {
    public BookSearchException(String message) {
        super(message);
    }

    public BookSearchException(String message, Throwable cause) {
        super(message);
    }
}
