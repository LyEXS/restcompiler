package lyes.restcompiler;

// Classe interne pour représenter chaque étape
public class Result {
    private boolean status;
    private String output;

    private boolean is_time_out;

    public boolean isIs_time_out() {
        return is_time_out;
    }

    public void setIs_time_out(boolean is_time_out) {
        this.is_time_out = is_time_out;
    }

    public Result() {
    }

    public Result(boolean status, String output) {
        this.status = status;
        this.output = output;

    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }
}