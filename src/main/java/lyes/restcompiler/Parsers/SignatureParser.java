package lyes.restcompiler.Parsers;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lyes.restcompiler.Exceptions.SignatureParsingException;
import lyes.restcompiler.Models.Signature;

public class SignatureParser {

    public static Signature parseSignature(String data) {
        Pattern p = Pattern.compile(
            "^\\s*(\\w[\\w\\*\\s]*?)\\s+(\\w+)\\s*\\(([^)]*)\\)\\s*$"
        );
        Matcher m = p.matcher(data);
        if (m.matches()) {
            Signature signature = new Signature();
            signature.setReturnType(m.group(1));
            signature.setFunctionName(m.group(2));
            signature.setParameters(Arrays.asList(m.group(3).split(",")));
            return signature;
        } else {
            throw new SignatureParsingException(
                "Erreur lors de l'analyse de la signature ",
                null
            );
        }
    }
}
