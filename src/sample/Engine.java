package sample;

import java.util.ArrayList;

import static sample.controller.MainController.*;
import static sample.model.Addition.createAdditions;
import static sample.model.Operandor.addOperand;
import static sample.model.Operandor.addOperator;

public class Engine {

    private static final String[] allOperators = {"+", "+=", "-", "-=", "*", "*=", "/", "/=", "%", "%=", "++", "--",
            "<", "<=", ">", ">=", "==", "!=", /*?*/".", ",", ";", "(", ")", "=", "&", "&=", "|", "|=", "~", "^", "^=",
            ">>", ">>=", "<<", "<<=", ">>>", "&&", "||", "!", "{", "}", "instanceof", "break", "continue", "switch",
            "if", "do", "while", "for", "return", "?"};

    private static final String[] otherReservedWords = {"byte", "short", "int", "long", "char", "float", "double",
            "boolean", "else", "case", "default", "try", "catch", "finally", "throw", "throws", "private", "protected",
            "public", "import", "package", "class", "interface", "extends", "implements", "static", "final", "void",
            "abstract", "native", "new", "this", "super", "synchronized", "volatile", "const", "goto", "enum", "assert",
            "transient", "strictfp"};

    private static final String[] methodHeaderSigns = {"private", "protected", "public", "static", "final", "abstract",
            "native", "strictfp"};

    public static void analyze() {
        operators = new ArrayList<>();
        operands = new ArrayList<>();
        additions = new ArrayList<>();


        boolean skip = false;
        int lineNo = 1;
        coder:
        for (String codeLine : code) {
            /* Проверка наличия содержимого строки */
            System.out.println("Line " + lineNo++ + ":");
            codeLine = codeLine.trim();
            if (codeLine.equals("")) continue;

            /* Удаление всех кавычек и содержимого между ними */
            while (codeLine.contains("\"")) {
                int textBegin = codeLine.indexOf("\"");
                int textEnd = codeLine.indexOf("\"", textBegin + 1) + 1;
                codeLine = codeLine.substring(0, textBegin) +
                        codeLine.substring(textEnd);
            }

            /* Распознавание заголовка функции или её вызова */
            boolean header;
            if ((header = isMethodHeader(codeLine)) || isMethodCall(codeLine)) {
                getMethodArgs(codeLine);
                if (header) addOperator("{");
                continue;
            }

            /* Все остальные случаи (начало распознавания отдельных слов) */
            for (String member : codeLine.split(" ")) {
                /* Пропуск слова после instanceof или class */
                if (skip) {
                    skip = false;
                    continue;
                }

                /* Частные случаи */
                switch (member) {
                    case "import":
                    case "package":
                        continue coder;
                    case "instanceof":
                        addOperator("instanceof");
//                        skip = true;
//                        continue;
                    case "class":
                        skip = true;
                        continue;
                }

                /* Общий случай */
                String operator = null;
                /* Проверка на оператор */
                if (isBelongsTo(member, allOperators)) {
                    addOperator(member);
                    continue;
                }
                /* 2-я попытка найти оператор в слове */
                else if (!isBelongsTo(member, otherReservedWords) && (operator = tryToFindOperator(member)) != null) {
                    addOperator(operator);
                    if (operator.equals("(")) member = member.substring(1);
                    while (member.startsWith("(")) {
                        member = member.substring(1);
                        addOperator("(");
                    }
                    if (operator.equals(")")) member = member.substring(0, member.length() - 1);
                    while (member.startsWith(")")) {
                        member = member.substring(0, member.length() - 1);
                        addOperator(")");
                    }
                }
//                if (member.startsWith("(") && operator != null) member = member.substring(1);
//                else if (operator != null) member = member.substring(0, member.length() - 1);
                if (!isBelongsTo(member, otherReservedWords) && !isBelongsTo(member, allOperators) &&
                        !member.equals("true") && !member.equals("false"))
                    addOperand(member);
            }
        }


        createAdditions();
    }

    private static String tryToFindOperator(String member) {
        if (member.endsWith(";")) return ";";
        if (member.endsWith(",")) return ",";
        if (member.endsWith(")")) return ")";
        if (member.startsWith("(")) return "(";
        if (member.endsWith(":"))
            member = member.substring(0, member.length() - 1);
        if (isBelongsTo(member, allOperators)) return member;
        else return null;
    }

    private static boolean isMethodHeader(String codeLine) {
        String[] lineMembers = codeLine.split(" ");
        if (lineMembers.length < 2 || !codeLine.contains("(") ||
                !codeLine.contains(")") || lineMembers[0].contains("(")) return false;
        boolean returnTypeDeclared = false;
        boolean paramsOpen = false;
        boolean operandDeclared = false;
        boolean methodOpened = false;
        for (String lineMember : lineMembers) {
            if (!isBelongsTo(lineMember, methodHeaderSigns)) {
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

    private static boolean isMethodCall(String codeLine) {
        boolean methodNameexists = false;
        if (!codeLine.contains("(") || !codeLine.contains(")")) return false;
        for (String word : codeLine.split(" ")) {
            if (word.endsWith("(")) methodNameexists = true;
            if (isBelongsTo(word, methodHeaderSigns)) return false;
        }
        return methodNameexists;
    }

    private static void getMethodArgs(String codeLine) {
        String[] args = codeLine.substring(codeLine.indexOf('(') + 1, codeLine.lastIndexOf(')')).split(" ");
        for (int i = 1; i < args.length; i += 2) {
            String operand = (args[i].charAt(args[i].length() - 1) == ',') ?
                    args[i].substring(0, args[i].length() - 1) : args[i];
            if (isMethodCall(operand)) getMethodArgs(operand);
            else addOperand(operand);
        }
    }

    private static boolean isBelongsTo(String x, String[] group) {
        for (String groupMember : group)
            if (x.equals(groupMember)) return true;
        return false;
    }
}