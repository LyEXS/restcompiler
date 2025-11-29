package org.lyes.restcompiler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileHandlers {
    public static boolean createCFile(String code){
        File cFile = null;
        try {
            cFile = new File("prog.c");
            BufferedWriter writer = new BufferedWriter(new FileWriter(cFile));
            writer.write(code);
            writer.close();
            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    public static boolean removeFile(String path){
        try {
            Files.delete(Path.of(path));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
