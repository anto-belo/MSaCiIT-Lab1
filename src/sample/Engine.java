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

    private static final String[] methodSigns = {"private", "protected", "public", "static", "final", "abstract",
                                                 "native", "strictfp"};

    public static void analyze() {
        operators = new ArrayList<>();
        operands = new ArrayList<>();
        additions = new ArrayList<>();

        boolean skip = false;
        coder:
        for (String codeLine : code) {
            codeLine = codeLine.trim();
            if (codeLine.equals("")) continue;

            if (isMethodHeader(codeLine)) {
                getMethodArgs(codeLine);
                continue;
            }
            for (String member : codeLine.trim().split(" ")) {
                if (skip) {
                    skip = false;
                    continue;
                }

                if (member.equals("import") || member.equals("package")) continue coder;
                else if (member.equals("class")) {
                    skip = true;
                    continue;
                }

                String operator;
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

    private static boolean isMethodHeader(String codeLine) {
        String[] lineMembers = codeLine.split(" ");
        if (lineMembers.length < 2 || !codeLine.contains("(") || !codeLine.contains(")")) return false;
        boolean returnTypeDeclared = false;
        boolean paramsOpen = false;
        boolean operandDeclared = false;
        boolean methodOpened = false;
        for (String lineMember : lineMembers) {
            if (!isInside(lineMember, methodSigns)) {
                if (!returnTypeDeclared)
                    returnTypeDeclared = true;
                else if (lineMember.contains("(") && !paramsOpen)
                    paramsOpen = true;
                else if (paramsOpen && lineMember.charAt(lineMember.length() - 1) == ',') {
                    operandDeclared = true;
                } else if (lineMember.contains(")") && paramsOpen) {
                    paramsOpen = false;
                } else if (operandDeclared)
                    operandDeclared = false;
                else if (!methodOpened && !paramsOpen && lineMember.equals("{"))
                    methodOpened = true;
                else return false;
            }
        }
        return true;
    }

    private static void getMethodArgs(String codeLine) {
        String[] args = codeLine.substring(codeLine.indexOf('(') + 1, codeLine.indexOf(')')).split(" ");
        for (int i = 1; i < args.length; i += 2) {
            String operand = (args[i].charAt(args[i].length() - 1) == ',') ?
                    args[i].substring(0, args[i].length() - 1) : args[i];
            addOperand(operand);
        }
        addOperator("{");
    }
    /* TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST */
    /*private static boolean tryParseMethodHeader(String codeLine) {
        String[] lineMembers = codeLine.split(" ");
        if (lineMembers.length < 2 || !codeLine.contains("(") || !codeLine.contains(")")) return false;
        boolean returnTypeDeclared = false;
        boolean paramsOpen = false;
        boolean operandAdded = false;
        for (String lineMember : lineMembers) {
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
    }*/

    private static boolean isInside(String x, String[] group) {
        for (String groupMember : group)
            if (x.equals(groupMember)) return true;
        return false;
    }
}