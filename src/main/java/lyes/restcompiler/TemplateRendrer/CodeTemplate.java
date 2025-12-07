package lyes.restcompiler.TemplateRendrer;

import java.util.List;
import lyes.restcompiler.Encoders.DataStrucuturesEncoder;
import lyes.restcompiler.Models.Request;
import lyes.restcompiler.Models.Signature;
import lyes.restcompiler.Models.TestCase;

public class CodeTemplate {

    public static String render(Request request) {
        // --- Récupération des données sécurisée ---
        Signature sig = request.getSignature();
        List<TestCase> testCases = request.getTestCases();
        List<String> params = sig.getParameters();
        String returnType = sig.getReturnType();

        if (returnType == null) {
            returnType = "int";
        }

        StringBuilder sb = new StringBuilder();

        // on inclut les bibliotheques
        sb.append("#include <stdio.h>\n");
        sb.append("#include <string.h>\n");
        sb.append("#include <stdlib.h>\n\n");
        sb.append("#include <ctype.h>\n\n");
        sb.append("#include <math.h>\n");
        sb.append("#include <stdbool.h>\n");

        // ici le code de la fonction fournie par la requete
        sb.append(request.getCode()).append("\n\n");

        // si la fonction retourne un tableau on déclare la fonction qui sert a sérialiser le tableau en chaine de caractere
        if (
            returnType.equals("int*") ||
            returnType.equals("float*") ||
            returnType.equals("double*") ||
            returnType.equals("long*") ||
            returnType.equals("char**") ||
            returnType.equals("bool*")
        ) {
            sb.append(
                DataStrucuturesEncoder.getArraySerializationFunctionCode(
                    returnType
                )
            );
        }

        // ici le main
        sb.append("int main(){\n");
        sb.append("int allPassed=0;\n");

        // --- ici on déclare le resultat, on a divisé la déclaration de chaine de char et autre types
        if ("char*".equals(returnType)) {
            sb.append("char *result;\n\n");
        } else {
            // Utiliser le returnType réel (int, float, double, etc.)
            sb.append(returnType).append(" result;\n\n");
        }

        int testIndex = 1;
        List<String> types = sig.getParameters();
        // ici on génère le code des tests cases a partir de la classe TestCasesGenerator
        sb = TestCasesGenerator.generateTestCases(
            sb,
            types,
            testIndex,
            params,
            testCases,
            returnType,
            sig,
            request.is_in_place()
        );

        // ici le programme retourne le nombre de tests non passées
        // en cas de succès le code retourne 0
        // dans le cas ou le code retourne un code supérieur a 100
        // ça signifie que le code n'est pas représentatif de nombre de tests échouée mais un problème lors de l'éxecution
        sb.append("return allPassed;\n");
        sb.append("}\n");
        System.out.println(sb.toString());
        return sb.toString();
    }
}
