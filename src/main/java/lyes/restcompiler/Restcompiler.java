package lyes.restcompiler;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.*;

// c'est ici que se passe toute la logique d'environnement
public class Restcompiler {

    // cette fonction créer un dossier temporaire puis met dedans un fichier source C et un .exe
    // lance la commande fireJail : voir la documentation ici https://man7.org/linux/man-pages/man1/firejail.1.html
    // avec des params,
    // puis elle renvois un résultat dépendant du code retour de notre programme
    public static CompilationResult runCommand(String code) {
        CompilationResult result = new CompilationResult();
        Path tempDir = null;
        Path sandboxDir = null;

        try {
            // créer le .C et et le .o
            tempDir = Files.createTempDirectory("c_run");
            Path source = tempDir.resolve("test.c");
            Path executable = tempDir.resolve("program");

            // la on écrit le code dans le fichier source
            Files.writeString(source, code);
            // ici on créer le processus qui éxecute la commande gcc,
            // à noter que la compilation s'éffectue dans l'environnement serveur car elle ne présente pas de problème de sécurité
            // il est probable qu'elle pose problème si plusieurs utilisateurs tentent de compiler en même temps
            // mais on gérera ça plus tard
            // le wrapping permet de remplacer les fonctions malloc, calloc, free et realloc par des fonctions personnalisées qui gérerons les allocations mémoires
            Process compile = new ProcessBuilder(
                "gcc",
                source.toString(),
                "-o",
                executable.toString(),
                "-Wl,--wrap,malloc",
                "-Wl,--wrap,calloc",
                "-Wl,--wrap,free",
                "-Wl,--wrap,realloc"
            )
                .redirectErrorStream(true)
                .start();

            // Note : Pensez à gérer le waitFor et l'analyse du code de sortie de la compilation ici.
            // bien sur ici on a un temps limite pour compilation, mais bon, c'est pas très nécessaire
            if (!compile.waitFor(5, TimeUnit.SECONDS)) {
                compile.destroyForcibly();
                result.getCompilation().setStatus(false);
                result.getCompilation().setOutput("Timeout compilation");
                return result;
            }
            // ici on lit le stdout du process de compilation
            String compileOutput = readOutput(compile.getInputStream());

            result.getCompilation().setStatus(compile.exitValue() == 0);
            result.getCompilation().setOutput(compileOutput);

            if (compile.exitValue() != 0) return result;

            // ici si la compilation s'est bien passée, alors on rend l'exécutable exécutable
            executable.toFile().setExecutable(true);

            // on crée le dossier sandbox dans tmp
            String sandboxName = "sandbox_" + System.currentTimeMillis();
            sandboxDir = Paths.get("/tmp", sandboxName);
            Files.createDirectories(sandboxDir);
            // on met l'exe dedans
            Path sandboxBinary = sandboxDir.resolve("program");
            Files.copy(
                executable,
                sandboxBinary,
                StandardCopyOption.REPLACE_EXISTING
            );
            sandboxBinary.toFile().setExecutable(true);

            // ici on lance le programme dans le sandbox
            Process exec = new ProcessBuilder(
                "firejail",
                "--quiet", // ça c'est éviter les logs indésirables
                "--noprofile",
                "--private=" + sandboxDir.toString(), // ça c'est pour limiter l'accès au autres dossiers
                "--net=none",
                "--noroot", // ne pas donner accès au commandes sudo
                "--seccomp.drop=fork,clone,vfork,execve,execveat", // ça c'est pour bloquer les appels système dangereux
                // Meme si il c'est exécuté dans un env securisé
                "--caps.drop=all",
                "--rlimit-cpu=2",
                "--deterministic-exit-code",
                "--deterministic-shutdown",
                "--rlimit-as=5m", // Limiter la mémoire a 0.5MB
                "./program" // le programme a executer
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
                // ignorer les message logs de firejail
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
                // gérer les codes retour de FireJail (TImeout, mémorie dépassée etcc)
                // la c'est un peu complexe ou j'ai l'impression que c'est pas détérministe,
                // je trouve que les codes retour sont pas toujours les memes selon l'environnement d'exec
                // par exemple quand je le lance sur google cloud j'ai des codes retour différents
                // donc il se peut que ça soit, pour l'instant, pas représentatif du type de retour,
                // mais on verra ça plus tard

                // Codes pour l'arrêt forcé par un signal (Mémoire, Accès Illégal)
                if (exitCode == 134 || exitCode == 137 || exitCode == 139) {
                    result.getExecution().setStatus(false);
                    result
                        .getExecution()
                        .setOutput(
                            "Échec: Limite de Mémoire/Ressources Dépassée (Code " +
                                exitCode +
                                ")" +
                                (exitCode == 137
                                    ? " (SIGKILL/rlimit-as)"
                                    : (exitCode == 139 ? " (SIGSEGV)" : "")) +
                                "\n" +
                                output.toString().trim()
                        );
                }
                // Codes pour le Timeout CPU (rlimit-cpu)
                else if (exitCode == 124 || exitCode == 152) {
                    result.getExecution().setStatus(false);
                    result.getExecution().setIs_time_out(true);
                    result
                        .getExecution()
                        .setOutput(
                            "Timeout CPU\nCode d'erreur : " +
                                exitCode +
                                "\n" +
                                output.toString().trim()
                        );
                }
                // Code pour le Blocage d'Appel Système (Seccomp)
                else if (exitCode == 159) {
                    result.getExecution().setStatus(false);
                    result
                        .getExecution()
                        .setOutput(
                            "Appel système bloqué par la politique de sécurité (Seccomp/Code " +
                                exitCode +
                                ")\n" +
                                output.toString().trim()
                        );
                }
                // Code de Succès
                else if (exitCode == 0) {
                    result.getExecution().setStatus(true);
                    result
                        .getExecution()
                        .setOutput(
                            "Tout les tests sont passées \n" +
                                output.toString().trim()
                        );
                }
                // Gestion des autres codes d'erreur (Erreur de test, Erreur de l'utilisateur)
                else {
                    result.getExecution().setStatus(false);
                    result
                        .getExecution()
                        .setOutput(
                            "Erreur du Programme Utilisateur - Code: " +
                                exitCode +
                                "\n" +
                                output.toString().trim()
                        );
                }
            } else {
                exec.destroyForcibly();
                result.getExecution().setStatus(false);
                result.getExecution().setIs_time_out(true);
                result
                    .getExecution()
                    .setOutput(
                        "Timeout global (Non terminé dans les 4 secondes)"
                    );
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
