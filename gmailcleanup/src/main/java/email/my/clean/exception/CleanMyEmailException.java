package email.my.clean.exception;

public class CleanMyEmailException extends RuntimeException {
    public CleanMyEmailException(String message) {
        super(message);
    }

    public CleanMyEmailException(String message, Throwable exception) {
        super(message, exception);
    }
}
