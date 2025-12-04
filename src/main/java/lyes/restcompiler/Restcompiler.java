package lyes.restcompiler;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.*;

public class Restcompiler {

    public static CompilationResult runCommand(String code) {
        CompilationResult result = new CompilationResult();
        Path tempDir = null;
        Path sandboxDir = null;

        try {
            tempDir = Files.createTempDirectory("c_run");
            Path source = tempDir.resolve("test.c");
            Path executable = tempDir.resolve("program");

            Files.writeString(source, code);

            Process compile = new ProcessBuilder(
                "gcc",
                source.toString(),
                "-o",
                executable.toString()
            )
                .redirectErrorStream(true)
                .start();

            if (!compile.waitFor(5, TimeUnit.SECONDS)) {
                compile.destroyForcibly();
                result.getCompilation().setStatus(false);
                result.getCompilation().setOutput("Timeout compilation");
                return result;
            }

            String compileOutput = readOutput(compile.getInputStream());
            result.getCompilation().setStatus(compile.exitValue() == 0);
            result.getCompilation().setOutput(compileOutput);

            if (compile.exitValue() != 0) return result;

            executable.toFile().setExecutable(true);

            String sandboxName = "sandbox_" + System.currentTimeMillis();
            sandboxDir = Paths.get("/tmp", sandboxName);
            Files.createDirectories(sandboxDir);

            Path sandboxBinary = sandboxDir.resolve("program");
            Files.copy(
                executable,
                sandboxBinary,
                StandardCopyOption.REPLACE_EXISTING
            );
            sandboxBinary.toFile().setExecutable(true);

            Process exec = new ProcessBuilder(
                "firejail",
                "--quiet",
                "--noprofile",
                "--private=" + sandboxDir.toString(),
                "--net=none",
                "--seccomp.drop=fork,clone,vfork,execve,execveat", // Bloque spécifiquement
                "--caps.drop=all",
                "--rlimit-cpu=2",
                "--rlimit-as=8m",
                "./program"
            )
                .directory(sandboxDir.toFile())
                .redirectErrorStream(true)
                .start();

            StringBuilder output = new StringBuilder();
            try (
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(exec.getInputStream())
                )
            ) {
                String line;
                while (
                    (line = reader.readLine()) != null && output.length() < 4096
                ) {
                    if (
                        !line.startsWith("Reading") &&
                        !line.startsWith("firejail") &&
                        !line.startsWith("Parent") &&
                        !line.startsWith("Child") &&
                        !line.startsWith("Base") &&
                        !line.startsWith("Warning")
                    ) {
                        output.append(line).append("\n");
                    }
                }
            }

            if (exec.waitFor(4, TimeUnit.SECONDS)) {
                int exitCode = exec.exitValue();

                if (exitCode == 0) {
                    result.getExecution().setStatus(true);
                    result.getExecution().setOutput(output.toString().trim());
                } else if (exitCode == 124 || exitCode == 152) {
                    result.getExecution().setStatus(false);
                    result.getExecution().setIs_time_out(true);
                    result
                        .getExecution()
                        .setOutput("Timeout CPU\n" + output.toString().trim());
                } else if (exitCode == 137) {
                    result.getExecution().setStatus(false);
                    result
                        .getExecution()
                        .setOutput(
                            "Timeout: Temps dépassé\n" +
                                output.toString().trim() +
                                "Code d'erreur : " +
                                exitCode
                        );
                } else if (exitCode == 159) {
                    result.getExecution().setStatus(false);
                    result
                        .getExecution()
                        .setOutput(
                            "Appel système bloqué\n" +
                                output.toString().trim() +
                                "Code d'erreur : " +
                                exitCode
                        );
                } else {
                    result.getExecution().setStatus(false);
                    result
                        .getExecution()
                        .setOutput(
                            "Erreur (code " +
                                exitCode +
                                ")\n" +
                                output.toString().trim() +
                                "Code d'erreur : " +
                                exitCode
                        );
                }
            } else {
                exec.destroyForcibly();
                result.getExecution().setStatus(false);
                result.getExecution().setIs_time_out(true);
                result.getExecution().setOutput("Timeout global");
            }
        } catch (Exception e) {
            result.getCompilation().setStatus(false);
            result.getCompilation().setOutput("Erreur: " + e.getMessage());
        } finally {
            cleanup(tempDir);
            cleanup(sandboxDir);
        }

        return result;
    }

    private static String readOutput(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (
            BufferedReader br = new BufferedReader(new InputStreamReader(is))
        ) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString().trim();
    }

    private static void cleanup(Path dir) {
        if (dir == null || !Files.exists(dir)) return;
        try {
            Files.walk(dir)
                .sorted((a, b) -> -a.compareTo(b))
                .map(Path::toFile)
                .forEach(File::delete);
        } catch (IOException ignored) {}
    }
}
