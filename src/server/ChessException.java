package server;

/**
 * A custom exception thrown from any of the Reversi classes if something
 * goes wrong.
 *
 * @author Andrei Tumbar
 */
public class ChessException extends Exception {
    /**
     * Convenience constructor to create a new {@link ChessException}
     * with an error message.
     *
     * @param msg The error message associated with the exception.
     */
    public ChessException(String msg) {
        super(msg);
    }

    /**
     * Convenience constructor to create a new {@link ChessException}
     * as a result of some other exception.
     *
     * @param cause The root cause of the exception.
     */
    public ChessException(Throwable cause) {
        super(cause);
    }

    /**
     * * Convenience constructor to create a new {@link ChessException}
     * as a result of some other exception.
     *
     * @param message The message associated with the exception.
     * @param cause The root cause of the exception.
     */
    public ChessException(String message, Throwable cause) {
        super(message, cause);
    }
}