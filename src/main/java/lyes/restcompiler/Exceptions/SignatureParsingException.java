package lyes.restcompiler.Exceptions;

public class SignatureParsingException extends RuntimeException {

    public SignatureParsingException(String message, Throwable err) {
        super(message, err);
    }
}
