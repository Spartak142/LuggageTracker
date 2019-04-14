package Server.integration;

public class BDBException extends Exception {
        // Create a new instance thrown because of the specified reason.
    public BDBException(String reason) {
        super(reason);
    }

    // Create a new instance thrown because of the specified reason and exception.
    public BDBException(String reason, Throwable rootCause) {
        super(reason, rootCause);
    }
}
