package lyes.restcompiler.Models;

import java.util.List;

public class Request {

    private Signature signature;
    private List<TestCase> testCases;
    private String returnType;
    private String code;

    public Request() {}

    public Request(
        Signature signature,
        List<TestCase> testCases,
        String returnType,
        String code
    ) {
        this.signature = signature;
        this.testCases = testCases;
        this.returnType = returnType;
        this.code = code;
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
