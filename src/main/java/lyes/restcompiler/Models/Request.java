package lyes.restcompiler.Models;

import java.util.List;

// ici la classe est la représentation objet du json fourni dans la requete
// elle permet de stocker les informations nécessaires à la compilation et à l'exécution du code
// il est nécessaire de bien respecter les noms et la casse des ses attributs
// je rappelle que le in_place est un boolean qui indique si la fonction fournie est une fonction in_place
public class Request {

    private Signature signature;
    private List<TestCase> testCases;
    private String returnType;
    private String code;
    private boolean is_in_place;

    public Request() {}

    public Request(
        Signature signature,
        List<TestCase> testCases,
        String returnType,
        String code,
        Boolean is_in_place
    ) {
        this.signature = signature;
        this.testCases = testCases;
        this.returnType = returnType;
        this.code = code;
        this.is_in_place = is_in_place;
    }

    public boolean is_in_place() {
        return is_in_place;
    }

    public void set_is_in_place(boolean is_in_place) {
        this.is_in_place = is_in_place;
    }

    public Signature getSignature() {
        return signature;
    }

    public List<TestCase> getTestCases() {
        return testCases;
    }

    public String getReturnType() {
        return returnType;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setSignature(Signature signature) {
        this.signature = signature;
    }

    public void setTestCases(List<TestCase> testCases) {
        this.testCases = testCases;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }
}
