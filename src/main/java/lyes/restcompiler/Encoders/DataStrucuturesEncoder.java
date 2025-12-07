package lyes.restcompiler.Encoders;

import java.util.List;

public class DataStrucuturesEncoder {

    private static String getBaseType(String arrayType) {
        if (arrayType.contains("long long")) {
            return "longlong";
        }
        return arrayType.replace("*", "").replace(" ", "");
    }

    private static String getBaseFunctionName(String arrayType) {
        return "serialize_" + getBaseType(arrayType) + "_array";
    }

    // C'est la fonction qui récup le nom de fonction adéquat pour l'appel dans le main
    public static String getSerializationFunctionName(String arrayType) {
        return getBaseFunctionName(arrayType);
    }

    // /**
    //  * Génère le code source C de la fonction de sérialisation d'un tableau,
    //  * qui alloue dynamiquement et retourne le char* sérialisé.
    //  */
    public static String getArraySerializationFunctionCode(String arrayType) {
        String functionName = getBaseFunctionName(arrayType);
        String functionSignature =
            "char* " + functionName + "(" + arrayType + " arr, int size)";
        String formatSpecifier = "";
        String baseType = getBaseType(arrayType);

        switch (baseType) {
            case "bool":
                formatSpecifier = "%s";
                break;
            case "int":
            case "long":
                formatSpecifier = "%ld";
                break;
            case "longlong":
                formatSpecifier = "%lld";
                break;
            case "float":
            case "double":
                formatSpecifier = "%.6f";
                break;
            case "char":
                formatSpecifier = "\"\\\"%s\\\"\"";
                break;
            default:
                return "// Type " + arrayType + " non supporté.\n";
        }

        // Le code C utilise malloc pour allouer le buffer de retour
        String cCode =
            functionSignature +
            " {\n" +
            "    size_t buffer_size = size * 20 + 10; // Estimation de la taille\n" +
            "    char* output_buffer = (char*)malloc(buffer_size);\n" +
            "\n" +
            "    if (output_buffer == NULL) return NULL;\n" +
            "\n" +
            "    if (arr == NULL || size <= 0) {\n" +
            "        strcpy(output_buffer, \"{}\");\n" +
            "        return output_buffer;\n" +
            "    }\n" +
            "    \n" +
            "    strcpy(output_buffer, \"{\");\n" +
            "    char element_str[256];\n" +
            "    \n" +
            "    for (int i = 0; i < size; i++) {\n";

        if ("bool".equals(baseType)) {
            // ici on converti le booléen en string (true/false)
            cCode +=
                "        const char* bool_val = arr[i] ? \"true\" : \"false\";\n" +
                "        sprintf(element_str, \"%s\", bool_val);\n";
        } else if (arrayType.equals("char**")) {
            // Logique pour char** (tableaux de chaînes)
            cCode +=
                "        sprintf(element_str, " +
                formatSpecifier +
                ", arr[i]);\n";
        } else {
            // Logique pour les numériques
            cCode +=
                "        sprintf(element_str, \"" +
                formatSpecifier +
                "\", arr[i]);\n";
        }

        cCode +=
            "        strcat(output_buffer, element_str);\n" +
            "        \n" +
            "        if (i < size - 1) {\n" +
            "            strcat(output_buffer, \", \");\n" +
            "        }\n" +
            "    }\n" +
            "    strcat(output_buffer, \"}\");\n" +
            "    return output_buffer;\n" +
            "}\n";

        return cCode;
    }
}
