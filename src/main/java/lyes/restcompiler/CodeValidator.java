package lyes.restcompiler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class CodeValidator {

    // Patterns pour détecter du code dangereux
    private static final Pattern[] DANGEROUS_PATTERNS = {
        // Appels système directs
        Pattern.compile(
            "\\bsystem\\s*\\(|\\bpopen\\s*\\(|\\bexec[lv]*\\s*\\(|\\bWinExec\\s*\\("
        ),
        // Manipulation de mémoire dangereuse
        Pattern.compile(
            "\\bgets\\s*\\(|\\bstrcpy\\s*\\(|\\bstrcat\\s*\\(|\\bsprintf\\s*\\("
        ),
        // Inclusions dangereuses
        Pattern.compile(
            "#\\s*include\\s*[<\"](windows\\.h|sys/wait\\.h|unistd\\.h|pthread\\.h|sys/socket\\.h|netinet/|dlfcn\\.h)[>\"]"
        ),
        // Appels réseau
        Pattern.compile(
            "\\bsocket\\s*\\(|\\bconnect\\s*\\(|\\bbind\\s*\\(|\\blisten\\s*\\("
        ),
        // Processus et threads
        Pattern.compile(
            "\\bfork\\s*\\(|\\bclone\\s*\\(|\\bpthread_create\\s*\\("
        ),
        // Fichiers et répertoires
        Pattern.compile(
            "\\bchdir\\s*\\(|\\bchroot\\s*\\(|\\bremove\\s*\\(|\\bunlink\\s*\\(|\\bopen\\s*\\(|\\bfopen\\s*\\("
        ),
        // Shell et commandes
        Pattern.compile(
            "/bin/(sh|bash|dash|zsh)|cmd\\.exe|powershell|/usr/bin/env"
        ),
        // Appels inline assembleur
        Pattern.compile("__asm__|asm\\s*\\(|\\b_asm\\b"),
        // Accès mémoire direct
        Pattern.compile("\\bmmap\\s*\\(|\\bmunmap\\s*\\(|\\bmprotect\\s*\\("),
        // Signaux
        Pattern.compile("\\bsignal\\s*\\(|\\bsigaction\\s*\\(|\\bkill\\s*\\("),
        // Bibliothèques externes dynamiques
        Pattern.compile("\\bdlopen\\s*\\(|\\bdlsym\\s*\\("),
        // Fonctions obsolètes/dangereuses
        Pattern.compile(
            "\\bscanf\\s*\\(|\\bvscanf\\s*\\(|\\bvprintf\\s*\\(|\\bfgets\\s*\\(|\\bfread\\s*\\(|\\bfwrite\\s*\\("
        ),
        // Tentatives de contournement
        Pattern.compile("__attribute__\\s*\\(\\(constructor\\)\\)|DllMain"),
        // Variables environnement
        Pattern.compile("\\bgetenv\\s*\\(|\\bputenv\\s*\\(|\\bsetenv\\s*\\("),
        // Appels réseau avancés
        Pattern.compile(
            "\\bgethostbyname\\s*\\(|\\bgethostbyaddr\\s*\\(|\\bgethostent\\s*\\("
        ),
        // Contexte utilisateur
        Pattern.compile(
            "\\bgetuid\\s*\\(|\\bgetgid\\s*\\(|\\bsetuid\\s*\\(|\\bsetgid\\s*\\("
        ),
        // Dossiers système
        Pattern.compile("\\.\\./|/etc/|/var/|/tmp/.*\\.(so|dll|exe)|/dev/"),
        // Patterns pour injections
        Pattern.compile("\\$\\(|`.*`|\\$\\{[^}]*\\}"),
        // Redirections de shell
        Pattern.compile("[|&><]\\s*\\w+|\\d*[><]&\\d*"),
        // Appels de script
        Pattern.compile("execl?p\\s*\\(|execv?p\\s*\\("),
        // Commandes système
        Pattern.compile(
            "\\b(chmod|chown|mount|umount|insmod|rmmod|iptables|service|systemctl)\\s+"
        ),
        // Boucles infinies dangereuses
        Pattern.compile(
            "while\\s*\\(\\s*1\\s*\\)\\s*\\{[^}]*sleep\\s*\\([^)]*\\)[^}]*\\}"
        ),
        // Tentatives de débordement
        Pattern.compile(
            "\\bmalloc\\s*\\(\\s*0x|\\bcalloc\\s*\\(\\s*[0-9]+\\s*,\\s*[0-9]{6,}|\\balloca?\\s*\\("
        ),
        // Pointeurs de fonction dangereux
        Pattern.compile(
            "\\(\\s*void\\s*\\*\\s*\\)\\s*\\([^)]*\\)\\s*=|signal\\s*\\([^,]+,\\s*SIG_IGN"
        ),
        // Mémoire partagée
        Pattern.compile("\\bshmget\\s*\\(|\\bshmat\\s*\\(|\\bshmdt\\s*\\("),
        // Sémaphores
        Pattern.compile("\\bsemget\\s*\\(|\\bsemop\\s*\\(|\\bsemctl\\s*\\("),
        // Appels système indirects via syscall
        Pattern.compile("\\bsyscall\\s*\\(|\\b__syscall\\s*\\("),
    };

    // Patterns spécifiques pour stdin/stdout/stderr dangereux
    private static final Pattern[] IO_PATTERNS = {
        // Accès direct aux descripteurs de fichier
        Pattern.compile("\\bfileno\\s*\\(\\s*(stdin|stdout|stderr)\\s*\\)"),
        Pattern.compile("\\b(STDIN_FILENO|STDOUT_FILENO|STDERR_FILENO)\\b"),
        Pattern.compile(
            "\\b(fread|fwrite|fputs|fputc|putc|putchar|fgetc|getc|getchar)\\s*\\("
        ),
        Pattern.compile("#\\s*define\\s+.*\\b(stdin|stdout|stderr)\\b"),
        Pattern.compile("\\b(freopen|fdopen|fileno)\\s*\\("),
        Pattern.compile("\\b(dup|dup2)\\s*\\("),
        Pattern.compile(
            "\\b(fflush|setbuf|setvbuf)\\s*\\(\\s*(stdin|stdout|stderr)"
        ),
        Pattern.compile("\\b(read|write)\\s*\\(\\s*[01]\\s*,"), // descripteurs 0 ou 1
        Pattern.compile("\\b(close|fclose)\\s*\\(\\s*(stdin|stdout|stderr)"),
        Pattern.compile(
            "\\b(clearerr|feof|ferror)\\s*\\(\\s*(stdin|stdout|stderr)"
        ),
        Pattern.compile("\\b(tcgetattr|tcsetattr|isatty)\\s*\\("),
        Pattern.compile("\\b(select|poll|epoll)\\s*\\("), // multiplexage d'E/S
        Pattern.compile("\\b(fcntl|ioctl)\\s*\\("),
        Pattern.compile("\\b(setlinebuf|setbuffer|setbuf)\\s*\\("),
        Pattern.compile("\\b(ungetc|ungetwc)\\s*\\("),
        Pattern.compile(
            "\\b(scanf|fscanf|sscanf|vscanf|vfscanf|vsscanf)\\s*\\("
        ),
        Pattern.compile(
            "\\b(printf|fprintf|sprintf|snprintf|vprintf|vfprintf|vsprintf|vsnprintf)\\s*\\([^)]*\\b(stdin|stdout|stderr)"
        ),
        Pattern.compile("\\b(pwrite|pread)\\s*\\("),
        Pattern.compile(
            "\\b(fseek|ftell|rewind|fgetpos|fsetpos)\\s*\\(\\s*(stdin|stdout|stderr)"
        ),
        Pattern.compile("\\b(tmpfile|tmpnam|tempnam)\\s*\\("),
        Pattern.compile("\\b(fdopen|fileno)\\s*\\("),
        Pattern.compile(
            "\\b(setbuf|setbuffer|setlinebuf|setvbuf)\\s*\\([^)]*NULL"
        ),
        Pattern.compile(
            "\\b(getc_unlocked|putc_unlocked|getchar_unlocked|putchar_unlocked)\\s*\\("
        ),
    };

    // Whitelist: fonctions autorisées (basique printf/puts sans stdin/stdout manipulation)
    private static final String[] ALLOWED_FUNCTIONS = {
        "printf",
        "puts",
        "putchar",
        "main",
        "return",
    };

    public static boolean isValid(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }

        // Vérifier la longueur
        if (code.length() > 10000) {
            // Limite à 10KB
            return false;
        }

        // Vérifier les patterns dangereux
        String normalizedCode = normalizeCode(code);

        for (Pattern pattern : DANGEROUS_PATTERNS) {
            if (pattern.matcher(normalizedCode).find()) {
                return false;
            }
        }

        // Vérifier les patterns E/S dangereux
        for (Pattern pattern : IO_PATTERNS) {
            if (pattern.matcher(normalizedCode).find()) {
                return false;
            }
        }

        // Vérifier que le code contient une fonction main
        if (
            !normalizedCode.contains("int main") &&
            !normalizedCode.contains("void main")
        ) {
            return false;
        }

        // Vérifier les inclusions
        if (containsDangerousIncludes(normalizedCode)) {
            return false;
        }

        return true;
    }

    private static String normalizeCode(String code) {
        // Supprimer les commentaires pour éviter les faux positifs
        String noComments = code
            .replaceAll("//.*", "")
            .replaceAll("/\\*.*?\\*/", "");

        // Convertir en minuscules pour les vérifications (sauf dans les chaînes)
        return noComments.toLowerCase();
    }

    private static boolean containsDangerousIncludes(String code) {
        // Vérifier les inclusions autorisées uniquement
        Pattern includePattern = Pattern.compile(
            "#\\s*include\\s*[<\"]\\s*([^>\"]+)\\s*[>\"]"
        );
        Matcher matcher = includePattern.matcher(code);

        while (matcher.find()) {
            String included = matcher.group(1).trim().toLowerCase();
            // Autoriser seulement stdio.h et stdlib.h de base
            if (
                !included.equals("stdio.h") &&
                !included.equals("stdlib.h") &&
                !included.equals("string.h") &&
                !included.equals("math.h")
            ) {
                return true;
            }
        }
        return false;
    }

    // Méthode utilitaire pour obtenir un message d'erreur détaillé
    public static Validation validateWithDetails(String code) {
        if (code == null || code.trim().isEmpty()) {
            return Validation.error("Code vide");
        }

        if (code.length() > 10000) {
            return Validation.error("Code trop long (max 10KB)");
        }

        String normalizedCode = normalizeCode(code);

        // Vérifier la fonction main
        if (
            !normalizedCode.contains("int main") &&
            !normalizedCode.contains("void main")
        ) {
            return Validation.error("Fonction main() requise");
        }

        // Vérifier les patterns dangereux avec détails
        for (Pattern pattern : DANGEROUS_PATTERNS) {
            Matcher matcher = pattern.matcher(normalizedCode);
            if (matcher.find()) {
                return Validation.error(
                    "Code dangereux détecté: " + matcher.group()
                );
            }
        }

        for (Pattern pattern : IO_PATTERNS) {
            Matcher matcher = pattern.matcher(normalizedCode);
            if (matcher.find()) {
                return Validation.error(
                    "Manipulation d'E/S interdite: " + matcher.group()
                );
            }
        }

        if (containsDangerousIncludes(normalizedCode)) {
            return Validation.error("Inclusion de bibliothèque non autorisée");
        }

        return Validation.success(); // Code valide
    }
}
