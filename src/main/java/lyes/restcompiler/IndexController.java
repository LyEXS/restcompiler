package lyes.restcompiler;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@RestController
public class IndexController {

    @GetMapping("/")
    public String index() {
        try {
            // Charge le fichier depuis classpath:static/
            ClassPathResource resource = new ClassPathResource("static/index.html");
            InputStream inputStream = resource.getInputStream();
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Retourne une page HTML simple si le fichier n'est pas trouvé
            return """
                                    <!DOCTYPE html>
                                    <html>
                                    <head>
                                        <title>C Compiler</title>
                                        <style>
                                            body { font-family: Arial, sans-serif; margin: 40px; }
                                            h1 { color: #333; }
                                            .example { background: #f5f5f5; padding: 15px; margin: 15px 0; border-radius: 5px; }
                                            code { background: #eee; padding: 2px 5px; }
                                        </style>
                                    </head>
                                    <body>
                                        <h1>🛠️ C Compiler REST API</h1>
                                        <p>Service de compilation C en ligne</p>

                                        <div class="example">
                                            <h3>📋 Utilisation :</h3>
                                            <p><code>GET /compile?code=votre_code_c</code></p>
                                        </div>

                                        <div class="example">
                                            <h3>🚀 Test rapide :</h3>
                                            <a href="/compile?code=int main(){ return 0; }">Compiler un programme simple</a>
                                        </div>

                                        <div class="example">
                                            <h3>📝 Exemple :</h3>
                                            <pre><code>#include &lt;stdio.h&gt;

                    int main() {
                        printf("Hello World!\\n");
                        return 0;
                    }</code></pre>
                                            <a href="/compile?code=%23include%20%3Cstdio.h%3E%0A%0Aint%20main()%20%7B%0A%20%20%20%20printf(%22Hello%20World!%5Cn%22);%0A%20%20%20%20return%200;%0A%7D">Tester cet exemple</a>
                                        </div>
                                    </body>
                                    </html>
                                    """;
        }
    }
}