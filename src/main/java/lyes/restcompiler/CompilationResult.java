package lyes.restcompiler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CompilationResult {

    private Result compilation;
    private Result execution;

    public CompilationResult() {
        this.compilation = new Result();
        this.execution = new Result();
    }

    public Result getCompilation() {
        return compilation;
    }

    public void setCompilation(Result compilation) {
        this.compilation = compilation;
    }

    public Result getExecution() {
        return execution;
    }

    public void setExecution(Result execution) {
        this.execution = execution;
    }

    // Sérialisation JSON
    public String toJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

}
