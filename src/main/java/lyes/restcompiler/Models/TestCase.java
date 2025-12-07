package lyes.restcompiler.Models;

import java.util.List;

// ça c'est la classe TestCase qui représente un cas de test
public class TestCase {

    private List<String> args;
    private String expected;

    public TestCase(List<String> args) {
        this.args = args;
    }

    public String getExpected() {
        return expected;
    }

    public void setExpected(String expected) {
        this.expected = expected;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public String toString() {
        return "TestCase{" + "args=" + args + ", expected=" + expected + '}';
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestCase testCase = (TestCase) o;
        return args.equals(testCase.args) && expected.equals(testCase.expected);
    }

    public int hashCode() {
        return args.hashCode() + expected.hashCode();
    }
}
