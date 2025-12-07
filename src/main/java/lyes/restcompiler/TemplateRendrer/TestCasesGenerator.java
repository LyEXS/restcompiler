package lyes.restcompiler.TemplateRendrer;

import java.util.List;
import lyes.restcompiler.Encoders.DataStrucuturesEncoder;
import lyes.restcompiler.Models.Signature;
import lyes.restcompiler.Models.TestCase;

public class TestCasesGenerator {

    public static StringBuilder generateTestCases(
        StringBuilder sb,
        List<String> types,
        int testIndex,
        List<String> params,
        List<TestCase> testCases,
        String returnType,
        Signature sig,
        boolean is_in_place
    ) {
        for (TestCase tc : testCases) {
            sb.append("// Test Case ").append(testIndex).append("\n");

            // ici pour chaque test case on déclare les parametres a l'avance
            List<String> args = tc.getArgs();
            int sizeArgIndex = params.size() - 1;

            for (int i = 0; i < params.size(); i++) {
                // l'argument sera de type arg puis le numéro de test _ numéro de paramètre
                String varName = "arg" + i + "_" + testIndex;
                String value = args.get(i);
                String type = types.get(i);

                // ici une logique pour gérer les guillemets de chaines de char
                if (
                    "char*".equals(type) &&
                    !"NULL".equals(value) &&
                    !value.startsWith("{")
                ) {
                    value = value.replace("\"", "");
                    value = "\"" + value + "\"";
                }
                value = value.replace("\\\\", "\\");

                // ici on déclare le dernier param comme int vu que c'est le param de la taille du tableau de sortie
                if (i == sizeArgIndex && type.equals("int*")) {
                    sb
                        .append("int ")
                        .append(varName)
                        .append(" = 0;")
                        .append("\n");
                } else {
                    sb
                        .append(
                            Declarator.generateCDeclaration(
                                type,
                                varName,
                                value,
                                false
                            )
                        )
                        .append("\n");
                }
            }
            // ------------------------------------------------------------------------------------------

            // ici je l'appelle de la fonction métier
            if (!"void".equals(returnType)) {
                sb.append("result=");
            }

            // Passage par Adresse (&)
            sb.append(sig.getFunctionName()).append("(");
            for (int i = 0; i < params.size(); i++) {
                String varName = "arg" + i + "_" + testIndex;
                String type = types.get(i);

                // Appliquer '&' UNIQUEMENT si le type de retour est un tableau ET que le paramètre est le pointeur de taille
                if (
                    returnType.endsWith("*") &&
                    i == sizeArgIndex &&
                    type.equals("int*")
                ) {
                    sb.append("&").append(varName);
                } else {
                    sb.append(varName);
                }

                if (i < params.size() - 1) sb.append(",");
            }
            sb.append(");\n");

            // ici on compare le résultat avec la valeur attendue
            if (!"void".equals(returnType)) {
                sb.append("{\n");
                sb.append("char resultStr[1024];\n");
                sb.append("char expectedStr[1024];\n");

                String expectedValue = tc.getExpected();
                expectedValue = expectedValue.replace("\"", "\\\"");

                sb
                    .append("strcpy(expectedStr,\"")
                    .append(expectedValue)
                    .append("\");\n");

                switch (returnType) {
                    case "int":
                    case "float":
                    case "double":
                    case "long":
                    case "bool":
                        // Cas numériques et booléens scalaires
                        if ("bool".equals(returnType)) {
                            // Conversion bool C (0 ou 1) en chaine "true" ou "false"
                            sb.append(
                                "const char* bool_str = result ? \"true\" : \"false\";\n"
                            );
                            sb.append("strcpy(resultStr, bool_str);\n");
                        } else {
                            // Logique pour les numériques
                            String format = "%d";
                            if ("float".equals(returnType)) format = "%.6f";
                            else if ("double".equals(returnType)) format =
                                "%.12lf";
                            else if ("long".equals(returnType)) format = "%ld";

                            sb
                                .append("sprintf(resultStr,\"")
                                .append(format)
                                .append("\",result);\n");
                        }
                        break;
                    case "char*":
                        // Libération conditionnelle du résultat pour char*
                        sb
                            .append("if (result != NULL) {\n")
                            .append(
                                "sprintf(resultStr, \"\\\"%s\\\"\", result);\n"
                            );
                        if (!is_in_place) {
                            sb.append("free(result);\n");
                        }
                        sb
                            .append("} else {\n")
                            .append("strcpy(resultStr,\"NULL\");\n")
                            .append("}\n");
                        break;
                    case "int*":
                    case "char**":
                    case "float*":
                    case "double*":
                    case "long*":
                    case "long long*":
                    case "bool*":
                        // Cas des tableaux retournés avec taille par référence
                        int sizeArgIndexCase = sig.getParameters().size() - 1;
                        String sizeVarName =
                            "arg" + sizeArgIndexCase + "_" + testIndex;

                        sb.append("if (result != NULL) {\n");

                        // 1. Appel du Sérialiseur C (retourne char* alloué)
                        sb
                            .append("char* result_tmp = ")
                            .append(
                                DataStrucuturesEncoder.getSerializationFunctionName(
                                    returnType
                                )
                            )
                            .append("(result, ")
                            .append(sizeVarName)
                            .append(");\n")
                            // 2. Copier le résultat sérialisé et nettoyer result_tmp
                            .append("strcpy(resultStr,result_tmp);\n")
                            .append("free(result_tmp);\n");

                        // 3. Libération conditionnelle du tableau de pointeurs
                        // si la fonction est in-place alors on libère pas car elle retourne un pointeur vers
                        // la valeur d'entrée et donc cette dernière est pas libérable
                        if (!is_in_place) {
                            sb.append("free(result);\n");
                        }

                        sb
                            .append("} else {\n")
                            .append("strcpy(resultStr,\"{}\");\n")
                            .append("}\n");
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
                sb.append("}\n"); // Fermeture du bloc de comparaison
            }

            testIndex++;
        }
        return sb;
    }
}
