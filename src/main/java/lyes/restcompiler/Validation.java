package lyes.restcompiler;

public class Validation {

    private String message;
    private boolean isValid;

    public Validation(String message, boolean isValid) {
        this.message = message;
        this.isValid = isValid;
    }

    public String getMessage() {
        return message;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    public static Validation error(String message) {
        return new Validation(message, false);
    }

    public static Validation success() {
        return new Validation(null, true);
    }
}
