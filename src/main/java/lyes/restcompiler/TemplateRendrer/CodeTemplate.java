package lyes.restcompiler.TemplateRendrer;

import java.util.List;
import lyes.restcompiler.Encoders.DataStrucuturesEncoder;
import lyes.restcompiler.Models.Request;
import lyes.restcompiler.Models.Signature;
import lyes.restcompiler.Models.TestCase;

public class CodeTemplate {

    // Définir la constante pour le code d'erreur de dépassement de limite mémoire
    private static final int MALLOC_FAILURE_EXIT_CODE = 101;

    // Code C du wrapper malloc pour intercepter les appels et retourner 101 en cas d'échec
    private static final String MALLOC_WRAPPER_CODE =
        "\n#define MALLOC_FAILURE_EXIT_CODE " +
        MALLOC_FAILURE_EXIT_CODE +
        "\n" +
        "\n// --- Wrapper Mémoire pour Détection MLE (Memory Limit Exceeded) ---\n" +
        "#include <unistd.h> // Nécessaire pour _exit()\n" +
        "extern void *__real_malloc(size_t);\n" +
        "extern void __real_free(void *);\n" +
        "extern void *__real_calloc(size_t, size_t);\n" +
        "extern void *__real_realloc(void *, size_t);\n" +
        "void* __wrap_malloc(size_t size) {\n" +
        "    void* ptr = __real_malloc(size);\n" +
        "    if (ptr == NULL) {\n" +
        "        fprintf(stderr, \"\\n\\n!!! ERREUR CRITIQUE: Dépassement de la limite de mémoire (MLE) lors de l'allocation de %zu octets.\\n\", size);\n" +
        "        // Sortie immédiate avec notre code spécifique, contournant le main()\n" +
        "        _exit(MALLOC_FAILURE_EXIT_CODE);\n" +
        "    }\n" +
        "    return ptr;\n" +
        "}\n" +
        "void* __wrap_calloc(size_t nmemb, size_t size) {\n" +
        "    void *ptr = __real_calloc(nmemb, size);\n" +
        "    if (ptr == NULL) {\n" +
        "        fprintf(stderr, \"\\n\\n!!! ERREUR CRITIQUE: Dépassement de la limite de mémoire (MLE) lors de l'allocation calloc.\\n\");\n" +
        "        _exit(MALLOC_FAILURE_EXIT_CODE);\n" +
        "    }\n" +
        "    return ptr;\n" +
        "}\n" +
        "void __wrap_free(void *ptr) {\n" +
        "    __real_free(ptr);\n" +
        "    // Pas de vérification d'erreur ici, juste l'appel à la fonction réelle\n" +
        "}\n" +
        "void* __wrap_realloc(void *ptr, size_t size) {\n" +
        "    void *new_ptr = __real_realloc(ptr, size);\n" +
        "    if (new_ptr == NULL) {\n" +
        "        fprintf(stderr, \"\\n\\n!!! ERREUR CRITIQUE: Dépassement de la limite de mémoire (MLE) lors de l'allocation realloc.\\n\");\n" +
        "        _exit(MALLOC_FAILURE_EXIT_CODE);\n" +
        "    }\n" +
        "    return new_ptr;\n" +
        "}\n\n" +
        "// --- Fin du Wrapper Mémoire ---\n\n";

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

        // 1. Inclusions des bibliothèques
        sb.append("#include <stdio.h>\n");
        sb.append("#include <string.h>\n");
        sb.append("#include <stdlib.h>\n");
        sb.append("#include <ctype.h>\n");
        sb.append("#include <math.h>\n");
        sb.append("#include <stdbool.h>\n");

        // 2. AJOUT DU WRAPPER MALLOC ICI
        sb.append(MALLOC_WRAPPER_CODE);

        // 3. Code de la fonction fournie par la requête (inclut le code utilisateur)
        sb.append(request.getCode()).append("\n\n");

        // 4. Fonction de sérialisation si besoin
        if (
            returnType.equals("int*") ||
            returnType.equals("float*") ||
            returnType.equals("double*") ||
            returnType.equals("long*") ||
            returnType.equals("char**") ||
            returnType.equals("bool*")
        ) {
            // Note: Assurez-vous que le code de sérialisation ne contient pas de malloc non vérifié.
            sb.append(
                DataStrucuturesEncoder.getArraySerializationFunctionCode(
                    returnType
                )
            );
        }

        // 5. Début du main()
        sb.append("int main(){\n");
        sb.append("int allPassed=0;\n");

        // ... Reste de votre logique de déclaration de variables ...
        if ("char*".equals(returnType)) {
            sb.append("char *result;\n\n");
        } else {
            sb.append(returnType).append(" result;\n\n");
        }

        int testIndex = 1;
        List<String> types = sig.getParameters();

        // 6. Génération des tests cases
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

        // 7. Retour du programme (allPassed sera ignoré si le wrapper a déjà appelé _exit(101))
        sb.append("return allPassed;\n");
        sb.append("}\n");
        System.out.println(sb.toString());
        return sb.toString();
    }
}
