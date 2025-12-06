package lyes.restcompiler.Encoders;

public class DataStrucuturesEncoder {

    public static String encodeIntArray(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "{}";
        }

        // Nettoyer: enlever les crochets et espaces
        input = input.trim();
        if (input.startsWith("[") && input.endsWith("]")) {
            input = input.substring(1, input.length() - 1);
        }

        if (input.trim().isEmpty()) {
            return "{}";
        }

        String[] parts = input.split(",");
        StringBuilder sb = new StringBuilder("{");

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            if (!part.isEmpty()) {
                sb.append(part);
                if (i < parts.length - 1) {
                    sb.append(", ");
                }
            }
        }
        sb.append("}");

        return sb.toString();
    }

    public static String encodeStringArray(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "NULL";
        }

        // Pour les tableaux de strings, on retourne juste la chaîne JSON
        // car elle sera parsée par create_string_array()
        return input;
    }
}
