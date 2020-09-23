package sample;

import java.util.ArrayList;

import static sample.controller.MainController.*;
import static sample.model.Addition.createAdditions;
import static sample.model.Operandor.addOperand;
import static sample.model.Operandor.addOperator;

public class Engine {

    private static final String[] allOperators = {"+", "+=", "-", "-=", "*", "*=", "/", "/=", "%", "%=", "++", "--",
                                                  "<", "<=", ">", ">=", "==", "!=", /*?*/".", ",", ";", "(", ")", "=",
                                                  "&", "&=", "|", "|=", "~", "^", "^=", ">>", ">>=", "<<", "<<=", ">>>",
                                                  "&&", "||", "!", "{", "}", "instanceof", "break", "continue",
                                                  "switch", "if", "do", "while", "for", "return", "?"};

    private static final String[] otherReservedWords = {"byte", "short", "int", "long", "char", "float", "double",
                                                        "boolean", "else", "case", "default", "try", "catch", "finally",
                                                        "throw", "throws", "private", "protected", "public", "import",
                                                        "package", "class", "interface", "extends", "implements",
                                                        "static", "final", "void", "abstract", "native", "new", "this",
                                                        "super", "synchronized", "volatile", "const", "goto", "enum",
                                                        "assert", "transient", "strictfp"};

    private static final String[] methodSigns = {"private", "protected", "public", "static", "final", "void",
                                                 "abstract", "native", "strictfp"};

    public static void analyze() {
        operators = new ArrayList<>();
        operands = new ArrayList<>();
        additions = new ArrayList<>();


        boolean skip = false;
        coder:
        for (String codeLine : code) {
            codeLine = codeLine.trim();
            if (codeLine.equals("") || tryParseMethodHeader(codeLine)) continue;
                for (String member : codeLine.trim().split(" ")) {
                    if (skip) {
                        skip = false;
                        continue;
                    }
                    String operator;

                    if (member.equals("import") || member.equals("package")) continue coder;
                    else if (member.equals("class")) {
                        skip = true;
                        continue;
                    }

                    if (isInside(member, allOperators))
                        addOperator(member);
                    else if (!isInside(member, otherReservedWords) && (operator = tryToFindOperator(member)) != null)
                        addOperator(operator);
                    else if (!isInside(member, otherReservedWords))
                        addOperand(member);
                }
        }


        createAdditions();
    }

    private static String tryToFindOperator(String member) {
        return null;
    }

    private static boolean tryParseMethodHeader(String codeLine) {
        String[] lineMembers = codeLine.split(" ");
        if (lineMembers.length < 2 || !codeLine.contains("(") || !codeLine.contains(")")) return false;
        boolean returnTypeDeclared = false;
        boolean paramsOpen = false;
        boolean operandAdded = false;
        for (String lineMember : lineMembers) {
            if (lineMember.equals("void")) returnTypeDeclared = true;
            if (!isInside(lineMember, methodSigns)) {
                if (!returnTypeDeclared)
                    returnTypeDeclared = true;
                else if (lineMember.contains("(") && !paramsOpen)
                    paramsOpen = true;
                else if (paramsOpen && lineMember.charAt(lineMember.length() - 1) == ',') {
                    addOperand(lineMember.substring(lineMember.length() - 1));
                    operandAdded = true;
                } else if (lineMember.contains(")") && paramsOpen) {
                    addOperand(lineMember.substring(0, lineMember.length() - 1));
                    paramsOpen = false;
                } else if (operandAdded)
                    operandAdded = false;
                else if (lineMember.equals("{"))
                    addOperator("{");
                else return false;
            }
        }
        return true;
    }

    private static boolean isInside(String x, String[] group) {
        for (String groupMember : group)
            if (x.equals(groupMember)) return true;
        return false;
    }
}