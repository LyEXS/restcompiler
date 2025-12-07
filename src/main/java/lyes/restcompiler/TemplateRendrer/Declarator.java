package lyes.restcompiler.TemplateRendrer;

public class Declarator {

    // ici la variable declared n'est pas utilisé parce que après quelques modifications j'ai fait en sorte
    // que a chaque test je crées des nouvelles variables, et donc decalred est false automatiquement
    public static String generateCDeclaration(
        String type,
        String varName,
        String value,
        boolean declared
    ) {
        // Nettoyage du type pour les cas complexes comme "long long*"
        String cleanType = type.trim();
        String baseType;

        // --- NOUVELLE GESTION ROBUSTE DES POINTEURS ---

        // 1. CAS GÉNÉRAL NULL (POUR TOUS LES POINTEURS : char*, int*, bool*, etc.)
        // Cela résout l'erreur "invalid initializer" lors de l'affectation de NULL à un tableau C.
        if (cleanType.endsWith("*") && value.equals("NULL")) {
            // Déclare comme un pointeur simple. Ex: int* argX_Y = NULL;
            return cleanType + " " + varName + " = NULL;";
        }

        // 2. CAS POINTEUR DE TAILLE (int* avec value="0" au dernier paramètre)
        // La logique est préservée de votre code d'origine.
        if (cleanType.equals("int*") && value.equals("0")) {
            return "int " + varName + " = 0;";
        }

        // ici le cas ou le type est bool
        if (cleanType.equals("bool")) {
            // Le C attend 'bool varName = true;'
            return "bool " + varName + " = " + value.toLowerCase() + ";";
        }

        // S'applique aux chaînes d'entrée pour les fonctions en place (reverse_string, etc.).
        if (cleanType.equals("char*")) {
            // La vérification NULL est faite en (1). Ici, on génère l'array modifiable.
            // Génère : char argX_Y[] = "valeur";
            return "char " + varName + "[] = " + value + ";";
        }

        // ici cas tableau de pointeurs / autrement dit un tableau de chaine de char
        if (cleanType.endsWith("**")) {
            // Si c'est char**, nous générons un tableau de pointeurs (char* varName[])
            String elementType = cleanType.replace("**", "*").trim();
            return elementType + " " + varName + "[]=" + value + ";";
        }

        // ici cas de tableaux numériques normaux int* float* etcc
        // Cette section gère aussi la déclaration spéciale du pointeur de taille (int* new_size).
        if (cleanType.endsWith("*")) {
            // cette logique permet juste des warnings chiants a la déclaration de tableaux vides
            if (value.equals("{}")) {
                value = "{0}"; // Utilise {0} au lieu de {}
            }

            // Déterminer le type de base pour la déclaration en tableau C
            if (cleanType.contains("long long")) {
                baseType = "long long";
            } else {
                baseType = cleanType.replace("*", "").trim();
            }

            // Pour les tableaux d'entrée int[], float[], bool[], etc.
            return baseType + " " + varName + "[]=" + value + ";";
        }

        // ici cas de type normal bool int float etcc
        return cleanType + " " + varName + "=" + value + ";";
    }
}
