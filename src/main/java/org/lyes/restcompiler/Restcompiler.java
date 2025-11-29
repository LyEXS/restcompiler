/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package org.lyes.restcompiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;

/**
 *
 * @author Lyexs
 */
public class Restcompiler {

    public static CompilationResult runCommand(String code) {
        CompilationResult result = new CompilationResult();
        StringBuilder outBuilder = new StringBuilder();

        if (!FileHandlers.createCFile(code)) {
            result.setOutput("Erreur lors de la création du fichier .c");
            result.setStatus(false);
            return result;
        }

        ProcessBuilder processBuilder = new ProcessBuilder(
                "gcc",
                "prog.c",
                "-o",
                "prog");
        processBuilder.redirectErrorStream(true);
        Process process = null;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            result.setOutput("Erreur lors de la création du processus");
            result.setStatus(false);
            return result;

        }
        assert process != null;

        try (BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = r.readLine()) != null) {
                outBuilder.append(line).append("\n");

            }
        } catch (IOException e) {
            process.destroyForcibly();
            result.setOutput("Erreur lors de la lecture du input stream");
            result.setStatus(false);
            return result;
        }

        try {

            boolean finished = process.waitFor(Duration.ofSeconds(5));
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("Temps d'éxecution limite dépassé");
            }
        } catch (InterruptedException e) {
            System.err.println("erreur, le processus a été interrompu avant sa fin ");
        }

        int exit = process.exitValue();
        result.setOutput(outBuilder.toString());
        result.setStatus(exit == 0);
        FileHandlers.removeFile("./prog.c");
        return result;

    }

    public static String runExec() {
        ProcessBuilder pb = new ProcessBuilder("./prog");
        pb.redirectErrorStream();
        Process process = null;
        try {
            process = pb.start();
        } catch (IOException e) {
            return "erreur lors de la creation du processus";
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }

        } catch (Exception e) {
            return "erreur lors de la lecture du output ";
        }

        FileHandlers.removeFile("./prog");
        return sb.toString();
    }

    public static void main(String[] args) {
        CompilationResult result = runCommand(
                """
                        #include <stdio.h>

                        int main() {
                            printf("Hello World!\\n");
                            return 0;
                        }
                        """);

        System.out.println(result.getStatus() ? "Compilation réussie " : "Erreur lors de compilation");
        System.out.println(result.getOutput());

        if (result.getStatus()) {
            String exec_out = runExec();
            System.out.println(exec_out);
        }
    }
}
