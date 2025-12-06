package lyes.restcompiler.BodyHandler;

import java.util.ArrayList;
import java.util.List;
import lyes.restcompiler.Models.Request;
import lyes.restcompiler.Models.Signature;
import lyes.restcompiler.Models.TestCase;
import org.json.JSONArray;
import org.json.JSONObject;

public class RequestHandler {

    public static Request handle(String input) {
        JSONObject json = new JSONObject(input);

        // --- FUNCTION FIELDS ---
        JSONObject function = json.getJSONObject("function");

        String code = function.getString("code");
        String signature_string = function.getString("signature");
        Signature signature =
            lyes.restcompiler.Parsers.SignatureParser.parseSignature(
                signature_string
            );

        String returnType = function.getString("return_type");
        JSONArray testCasesJson = json.getJSONArray("test_cases");
        List<TestCase> testCasesList = new ArrayList<>();

        for (int i = 0; i < testCasesJson.length(); i++) {
            JSONObject tcJson = testCasesJson.getJSONObject(i);

            // Extract args
            JSONArray argsJson = tcJson.getJSONArray("args");
            List<String> argsList = new ArrayList<>();

            for (int j = 0; j < argsJson.length(); j++) {
                JSONObject argObj = argsJson.getJSONObject(j);
                argsList.add(argObj.getString("value"));
            }

            // Extract expected
            String expected = tcJson.getString("expected");

            // Create TestCase object
            TestCase testCase = new TestCase(argsList);
            testCase.setExpected(expected);

            testCasesList.add(testCase);
        }
        Request request = new Request();
        request.setSignature(signature);
        request.setReturnType(returnType);
        request.setTestCases(testCasesList);
        request.setCode(code);
        return request;
    }
}
