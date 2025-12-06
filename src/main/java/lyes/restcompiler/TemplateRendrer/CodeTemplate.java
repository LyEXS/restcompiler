package lyes.restcompiler.TemplateRendrer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import lyes.restcompiler.Exceptions.JsonParsingException;
import lyes.restcompiler.Models.Request;
import lyes.restcompiler.Models.Signature;
import lyes.restcompiler.Models.TestCase;

public class CodeTemplate {

    public static String render(Request request) {
        StringBuilder sb = new StringBuilder();

        // --- Include standard headers (chaque #include sur une ligne séparée) ---
        sb.append("#include <stdio.h>\n");
        sb.append("#include <string.h>\n");
        sb.append("#include <stdlib.h>\n\n");

        // --- Function code ---
        sb.append(request.getCode()).append("\n\n");

        // --- Main function with tests ---
        sb.append("int main(){\n");
        sb.append("int allPassed=0;\n");
        sb.append("int result;\n\n");
        List<TestCase> testCases = null;
        try {
            testCases = request.getTestCases();
        } catch (JsonParsingException e) {
            throw new JsonParsingException(
                "Le parsing des test cases n'a pas pu etre effectué ",
                null
            );
        }
        Signature sig = null;
        try {
            sig = request.getSignature();
        } catch (JsonParsingException e) {
            throw new JsonParsingException(
                "Le parsing de la signature n'a pas pu etre effectué ",
                null
            );
        }
        String returnType;
        try {
            returnType = request.getReturnType();
        } catch (JsonParsingException e) {
            throw new JsonParsingException(
                "Le parsing du type de retour n'a pas pu etre effectué ",
                null
            );
        }
        List<String> params = null;
        try {
            params = sig.getParameters();
        } catch (JsonParsingException e) {
            throw new JsonParsingException(
                "Le parsing des paramètres n'a pas pu etre effectué ",
                null
            );
        }
        // for (int i = 0; i < params.size(); i++) {
        //     boolean is_array = false;
        //     String type = params.get(i);
        //     if (type.contains("*")) is_array = true;
        //     String varName = "arg" + i;
        //     // si i=0 on est a la première itération et donc il faut déclarer le type d'ou le i == 0
        //     sb
        //         .append(
        //             generateCDeclaration(
        //                 type,
        //                 varName,
        //                 is_array ? "NULL" : "0",
        //                 i == 0
        //             )
        //         )
        //         .append("\n");
        //     is_array = false;
        // }

        int testIndex = 1;
        List<String> types = sig.getParameters();
        for (TestCase tc : testCases) {
            sb.append("// Test Case ").append(testIndex).append("\n");

            // --- Declare arguments ---
            List<String> args = tc.getArgs();
            for (int i = 0; i < params.size(); i++) {
                String varName = "arg" + i + "_" + testIndex;
                String value = args.get(i);
                String type = types.get(i);
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                    // Maintenant value = "{'h','e','l','l','o','\\\\0'}"
                }

                // Si vous voulez aussi gérer le double backslash
                value = value.replace("\\\\", "\\");
                // Maintenant value = "{'h','e','l','l','o','\0'}"

                // le dernier param de generate dis que si la variable qu'on veux générer est déja déclarée et ou pas
                // après un changement, les variables ne sont jamais reaffectées, donc le param et false tout le temps

                sb
                    .append(generateCDeclaration(type, varName, value, false))
                    .append("\n");
            }

            // --- Call function ---
            if (!returnType.equals("void")) {
                sb.append("result=");
            }
            sb.append(sig.getFunctionName()).append("(");
            for (int i = 0; i < params.size(); i++) {
                sb.append("arg").append(i).append("_").append(testIndex);
                if (i < params.size() - 1) sb.append(",");
            }
            sb.append(");\n");

            // --- Compare result with expected ---
            if (!returnType.equals("void")) {
                sb.append("{\n");
                sb.append("char resultStr[1024];\n");
                sb.append("char expectedStr[1024];\n");
                sb
                    .append("strcpy(expectedStr,\"")
                    .append(tc.getExpected())
                    .append("\");\n");

                switch (returnType) {
                    case "int":
                        sb.append("sprintf(resultStr,\"%d\",result);\n");
                        break;
                    case "float":
                        sb.append("sprintf(resultStr,\"%f\",result);\n");
                        break;
                    case "double":
                        sb.append("sprintf(resultStr,\"%lf\",result);\n");
                        break;
                    case "char*":
                        sb.append("strcpy(resultStr,result);\n");
                        break;
                    default:
                        sb.append("strcpy(resultStr,\"\");\n");
                }

                sb.append("if(strcmp(resultStr,expectedStr)==0)\n");
                sb
                    .append("printf(\"Test")
                    .append(testIndex)
                    .append(" passed\\n\");\n");
                sb.append("else{\n");
                sb
                    .append("printf(\"Test")
                    .append(testIndex)
                    .append(
                        " failed : expected '%s', got '%s'\\n\",expectedStr,resultStr);\n"
                    );
                sb.append("allPassed++;\n");
                sb.append("}\n");
                sb.append("}\n");
            }

            testIndex++;
        }

        sb.append("return allPassed;\n");
        sb.append("}\n");
        System.out.println(sb.toString());
        return sb.toString();
    }

    private static String generateCDeclaration(
        String type,
        String varName,
        String value,
        boolean declared
    ) {
        if (type == null) {
            // Pas de type, juste l'assignation
            return varName + "=" + value + ";";
        }

        switch (type) {
            case "int":
            case "float":
            case "double":
            case "long":
            case "short":
                // Si la variable est déjà déclarée, on initialise sa valeur, sinon on déclare la variable.
                return declared
                    ? varName + "=" + value + ";"
                    : type + " " + varName + "=" + value + ";";
            case "char*":
                // Cas des pointeurs de type char* : soit on assigne une valeur si déjà déclarée, soit on déclare le pointeur.
                return declared
                    ? varName + "=" + value + ";"
                    : "char* " + varName + "[]=" + value + ";";
            case "int*":
            case "float*":
                // Cas des pointeurs : soit on assigne une valeur si déclarée, soit on déclare le pointeur.
                return declared
                    ? varName + "=" + value + ";"
                    : type.replace("*", "") +
                      " " +
                      varName +
                      "[]" +
                      "=" +
                      value +
                      ";";
            case "char**":
                // Cas des tableaux de char* : déclaration complexe qui nécessite un traitement particulier.
                return (
                    "// Déclaration complexe de tableau de char* " +
                    varName +
                    " (" +
                    type +
                    ") à gérer manuellement;"
                );
            case "LinkedList<int>":
                // Cas spécifique pour les structures de type générique comme LinkedList
                return (
                    "// Déclaration complexe de LinkedList<int> " +
                    varName +
                    " à gérer manuellement;"
                );
            default:
                // Si le type est inconnu ou non supporté
                return "// Unsupported type " + type + ";";
        }
    }
}
