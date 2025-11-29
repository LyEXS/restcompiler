package lyes.restcompiler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileHandlers {
    private static final Logger logger = LoggerFactory.getLogger(FileHandlers.class);

    /**
     * Crée un fichier C dans le dossier donné
     * 
     * @param code   Le code C à écrire
     * @param fileId Nom de fichier sans extension
     * @param dir    Dossier où créer le fichier
     * @return true si succès
     */
    public static boolean createCFile(String code, String fileId, String dir) {
        File folder = new File(dir);
        if (!folder.exists())
            folder.mkdirs();

        File cFile = new File(folder, fileId + ".c");
        logger.info("Création fichier: {}", cFile.getAbsolutePath());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(cFile))) {
            writer.write(code);
            logger.info("Fichier C créé avec succès");
            return true;
        } catch (Exception e) {
            logger.error("Erreur création fichier C: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Supprime un fichier
     * 
     * @param path Chemin complet du fichier
     * @return true si succès
     */
    public static boolean removeFile(String path) {
        logger.info("Suppression fichier: {}", path);
        try {
            Files.delete(Path.of(path));
            logger.info("Fichier supprimé: {}", path);
            return true;
        } catch (Exception e) {
            logger.warn("Échec suppression {}: {}", path, e.getMessage());
            return false;
        }
    }
}
