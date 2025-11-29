package org.lyes.restcompiler;

public class CompilationResult {
    private String output; 
    private boolean status;
    public String getOutput() {
        return output;
    }
    public CompilationResult() {
    }
    public void setOutput(String output) {
        this.output = output;
    }
    public boolean getStatus() {
        return status;
    }
    public void setStatus(boolean status) {
        this.status = status;
    }
    public CompilationResult(String output, boolean status) {
        this.output = output;
        this.status = status;
    }
    
}
