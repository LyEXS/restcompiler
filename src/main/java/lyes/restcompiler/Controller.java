package lyes.restcompiler;

import lyes.restcompiler.Exceptions.JsonParsingException;
import lyes.restcompiler.Models.Request;
import lyes.restcompiler.TemplateRendrer.CodeTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    @PostMapping("/test")
    public String test(@RequestBody Request payload) {
        try {
            return CodeTemplate.render(payload);
        } catch (JsonParsingException e) {
            return "Error: " + e.getMessage();
        }
    }

    // requete post qui va retourner le résultat de la compilation du code
    @PostMapping("/compile")
    public ResponseEntity<CompilationResult> compile(
        @RequestBody Request request
    ) {
        String code = CodeTemplate.render(request);

        // Validation validation = CodeValidator.validateWithDetails(code);
        // if (!validation.isValid()) {
        //     CompilationResult errorResult = new CompilationResult();
        //     errorResult.getCompilation().setStatus(false);
        //     errorResult.getCompilation().setOutput(validation.getMessage());
        //     errorResult.getExecution().setStatus(false);
        //     errorResult.getExecution().setOutput("");
        //     return ResponseEntity.badRequest().body(errorResult);
        // }

        // validation a voir apres

        if (code == null || code.trim().isEmpty()) {
            CompilationResult errorResult = new CompilationResult();
            errorResult.getCompilation().setStatus(false);
            errorResult
                .getCompilation()
                .setOutput("Le paramètre 'code' est requis");
            errorResult.getExecution().setStatus(false);
            errorResult.getExecution().setOutput("");
            return ResponseEntity.badRequest().body(errorResult);
        }

        CompilationResult result = Restcompiler.runCommand(code);
        if (result == null) {
            CompilationResult errorResult = new CompilationResult();
            errorResult.getCompilation().setStatus(false);
            errorResult
                .getCompilation()
                .setOutput("Erreur interne: résultat null");
            errorResult.getExecution().setStatus(false);
            errorResult.getExecution().setOutput("");
            return ResponseEntity.internalServerError().body(errorResult);
        }

        return ResponseEntity.ok(result);
    }
}
