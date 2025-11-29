package lyes.restcompiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lyes.restcompiler.Result;

public class Restcompiler {

    private static final String SANDBOX_DIR = System.getProperty("user.home")
            + "/Documents/ProjetsWeb/restcompiler/podman-sandbox/tmp";
    private static final String IMAGE_NAME = "sandbox-c";

    public static CompilationResult runCommand(String code) {
        String fileId = generateUniqueID();
        CompilationResult result = new CompilationResult();

        // Crée le dossier sandbox s'il n'existe pas
        File tmpDir = new File(SANDBOX_DIR);
        if (!tmpDir.exists())
            tmpDir.mkdirs();

        // Création du fichier C
        String filePath = SANDBOX_DIR + "/" + fileId + ".c";
        if (!FileHandlers.createCFile(code, fileId, SANDBOX_DIR)) {
            result.getCompilation().setStatus(false);
            result.getCompilation().setOutput("Erreur lors de la création du fichier .c");
            return result;
        }

        // 1️⃣ Compilation dans Podman
        String compileCmd = String.format("gcc /sandbox/%s.c -o /sandbox/%s.out", fileId, fileId);
        Result compilationResult = executeInPodman(compileCmd);
        result.setCompilation(compilationResult);

        if (!compilationResult.isStatus()) {
            // Si compilation échoue, ne pas exécuter
            result.getExecution().setStatus(false);
            result.getExecution().setOutput("");
            cleanup(filePath, SANDBOX_DIR + "/" + fileId + ".out");
            return result;
        }

        // 2️⃣ Exécution dans Podman
        String execCmd = String.format("timeout 3s /sandbox/%s.out", fileId);
        Result executionResult = executeInPodman(execCmd);
        result.setExecution(executionResult);

        // Nettoyage
        cleanup(filePath, SANDBOX_DIR + "/" + fileId + ".out");

        return result;
    }

    private static Result executeInPodman(String command) {
        Result result = new Result();
        String[] cmd = {
                "podman", "run", "--rm",
                "-v", SANDBOX_DIR + ":/sandbox:Z",
                IMAGE_NAME,
                "bash", "-c", command
        };

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);

        StringBuilder output = new StringBuilder();
        Process process = null;

        try {
            process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            boolean finished = process.waitFor(5, TimeUnit.SECONDS); // <-- peut lancer InterruptedException
            int exitCode;
            if (!finished) {
                process.destroyForcibly();
                exitCode = 124;
            } else {
                exitCode = process.exitValue();
            }

            if (exitCode == 124) {
                result.setStatus(false);
                result.setOutput("Timeout dépassé");
                result.setIs_time_out(true);
            } else {
                result.setStatus(exitCode == 0);
                result.setOutput(output.toString());
            }

        } catch (IOException | InterruptedException e) {
            result.setStatus(false);
            result.setOutput("Erreur: " + e.getMessage());
        }

        return result;
    }

    private static void cleanup(String... paths) {
        for (String path : paths) {
            FileHandlers.removeFile(path);
        }
    }

    private static String generateUniqueID() {
        String unique = UUID.randomUUID().toString();
        String fileId = unique.substring(7);
        Random r = new Random();
        char c = (char) (r.nextInt(26) + 'a');
        fileId = c + fileId.substring(1);
        return fileId;
    }
}
