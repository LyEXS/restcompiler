package lyes.restcompiler.Exceptions;

// ça c'est une classe qui représente une exception lors de la parsing d'une signature de fonction
// pas utilisé pour l'instant, peut etre plus tard
public class SignatureParsingException extends RuntimeException {

    public SignatureParsingException(String message, Throwable err) {
        super(message, err);
    }
}
