package lyes.restcompiler.Generators;

public class PrintGenerator {

    /**
     * Génère le code C pour un printf adapté au type fourni.
     *
     * @param type String représentant le type d'entrée : "int", "float", "char*", "int[]", "float[]", etc.
     * @param variableName nom de la variable à afficher
     * @return le code C sous forme de String
     */
    public static String generatePrint(String type, String variableName) {
        switch (type) {
            case "int":
                return printInt(variableName);
            case "float":
                return printFloat(variableName);
            case "double":
                return printDouble(variableName);
            case "char*":
                return printString(variableName);
            case "long":
                return printLong(variableName);
            case "short":
                return printShort(variableName);
            case "int[]":
                return printIntArray(variableName);
            case "float[]":
                return printFloatArray(variableName);
            case "char*[]":
                return printStringArray(variableName);
            case "LinkedList<int>":
                return printIntLinkedList(variableName);
            default:
                return "// Type non supporté : " + type;
        }
    }

    private static String printInt(String var) {
        return "printf(\"%d\", " + var + ");";
    }

    private static String printFloat(String var) {
        return "printf(\"%f\", " + var + ");";
    }

    private static String printDouble(String var) {
        return "printf(\"%lf\", " + var + ");";
    }

    private static String printString(String var) {
        return "printf(\"%s\", " + var + ");";
    }

    private static String printLong(String var) {
        return "printf(\"%ld\", " + var + ");";
    }

    private static String printShort(String var) {
        return "printf(\"%hd\", " + var + ");";
    }

    private static String printIntArray(String var) {
        return """
        for(int i=0;i<sizeof(%s)/sizeof(%s[0]);i++) printf("%%d ", %s[i]);
        """.formatted(var, var, var);
    }

    private static String printFloatArray(String var) {
        return """
        for(int i=0;i<sizeof(%s)/sizeof(%s[0]);i++) printf("%%f ", %s[i]);
        """.formatted(var, var, var);
    }

    private static String printStringArray(String var) {
        return """
        for(int i=0;i<sizeof(%s)/sizeof(%s[0]);i++) printf("%%s ", %s[i]);
        """.formatted(var, var, var);
    }

    private static String printIntLinkedList(String var) {
        return """
        {
            Node* current = %s;
            while(current != NULL) {
                printf("%%d ", current->val);
                current = current->next;
            }
        }
        """.formatted(var);
    }

    // --- TEST RAPIDE ---
    public static void main(String[] args) {
        System.out.println(generatePrint("int", "x"));
        System.out.println(generatePrint("float", "f"));
        System.out.println(generatePrint("char*", "str"));
        System.out.println(generatePrint("double", "d"));
        System.out.println(generatePrint("long", "l"));
        System.out.println(generatePrint("short", "s"));
        System.out.println(generatePrint("bool", "b"));
        System.out.println(generatePrint("int[]", "arr"));
        System.out.println(generatePrint("float[]", "farr"));
        System.out.println(generatePrint("char*[]", "sarr"));
        System.out.println(generatePrint("LinkedList<int>", "list"));
    }
}
