package lyes.restcompiler.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gère les erreurs de format JSON (Syntaxe ou type de données invalide)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleJsonErrors(
        HttpMessageNotReadableException ex
    ) {
        // Tente d'extraire le message d'erreur plus précis de Jackson
        String errorMessage =
            "Erreur de format JSON ou type de données invalide.";

        if (ex.getCause() != null) {
            // Jackson stocke souvent les détails de l'erreur dans la cause
            String causeMessage = ex.getCause().getMessage();

            if (causeMessage.contains("Unrecognized token")) {
                errorMessage =
                    "Erreur de syntaxe JSON : caractère ou mot-clé non reconnu.";
            } else if (
                causeMessage.contains("Cannot deserialize value of type")
            ) {
                // Ex: "Cannot deserialize value of type `boolean` from `null`"
                errorMessage =
                    "Type de données invalide. Vérifiez si une valeur est NULL ou du mauvais type (ex: chaîne au lieu d'un nombre/booléen).";
            } else if (causeMessage.contains("Unrecognized character escape")) {
                // Ex: "Unrecognized character escape '0' (code 48)"
                errorMessage =
                    "Caractère d'échappement C invalide dans une chaîne JSON (ex: '\\0' est interdit).";
            }
        }

        // Retourne une réponse 400 Bad Request
        return new ResponseEntity<>(
            "Erreur de désérialisation de la requête. " + errorMessage,
            HttpStatus.BAD_REQUEST
        );
    }
}
