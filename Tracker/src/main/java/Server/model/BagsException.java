package Server.model;

public class BagsException extends Exception{
   

    // Create a new instance thrown because of the specified reason.
    public BagsException(String reason) {
        super(reason);
    }

    // Create a new instance thrown because of the specified reason and exception.
    public BagsException(String reason, Throwable rootCause) {
        super(reason, rootCause);
    }
}
