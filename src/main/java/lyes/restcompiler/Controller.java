package lyes.restcompiler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class Controller {

    @PostMapping("/compile")
    public ResponseEntity<CompilationResult> compile(@RequestBody Map<String, String> payload) {
        String code = payload.get("code");

        if (code == null || code.trim().isEmpty()) {
            CompilationResult errorResult = new CompilationResult();
            errorResult.getCompilation().setStatus(false);
            errorResult.getCompilation().setOutput("Le paramètre 'code' est requis");
            errorResult.getExecution().setStatus(false);
            errorResult.getExecution().setOutput("");
            return ResponseEntity.badRequest().body(errorResult);
        }

        CompilationResult result = Restcompiler.runCommand(code);

        if (result == null) {
            CompilationResult errorResult = new CompilationResult();
            errorResult.getCompilation().setStatus(false);
            errorResult.getCompilation().setOutput("Erreur interne: résultat null");
            errorResult.getExecution().setStatus(false);
            errorResult.getExecution().setOutput("");
            return ResponseEntity.internalServerError().body(errorResult);
        }

        return ResponseEntity.ok(result);
    }
}
