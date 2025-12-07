package lyes.restcompiler.Models;

import java.util.List;

// ça c'est la classe qui représente une signature de fonction
public class Signature {

    private String returnType;
    private String functionName;
    private List<String> parameters;

    public Signature() {}

    public Signature(
        String returnType,
        String functionName,
        List<String> parameters
    ) {
        this.returnType = returnType;
        this.functionName = functionName;
        this.parameters = parameters;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return (
            "Signature{" +
            "returnType='" +
            returnType +
            '\'' +
            ", functionName='" +
            functionName +
            '\'' +
            ", parameters=" +
            parameters +
            '}'
        );
    }
}
