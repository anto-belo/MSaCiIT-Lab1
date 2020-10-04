package sample;

import java.util.ArrayList;

import static sample.controller.MainController.*;
import static sample.model.Addition.createAdditions;
import static sample.model.Operandor.addOperand;
import static sample.model.Operandor.addOperator;

public class Engine {

    private static final String[] allOperators = {"+=", "++", "+", "-=", "--", "-", "*=", "*", "/=", "/", "%=", "%",
            "&=", "&&", "&", "|=", "||", "|", "^=", "^", ">>=", ">>", "<<=", "<<", ">>>", "~", "<=", "<", ">=", ">",
            "!=", "==", "=", /*".",*/ ",", ";", "(", ")", "!", "{", "}", "instanceof", "break", "continue", "switch",
            "if", "do", "while", "for", "return", "?", ":"};

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

        int lineNo = 1;
        nextLine:
        for (String codeLine : code) {
            System.out.println("Line " + lineNo++ + ":");
            codeLine = formatCodeLine(codeLine);
            if (codeLine == null) continue;

            boolean isMethodHeader = isMethodHeader(codeLine);
            boolean skip = false;
            String[] lineMembers = codeLine.split(" ");
            for (int i = 0; i < lineMembers.length; i++) {
                String member = lineMembers[i];
                if (member.equals("")) continue;
                if (!isMethodHeader) isMethodHeader = isMethodHeader(codeLine.substring(codeLine.indexOf(member)));

                if (skip) {
                    skip = false;
                    continue;
                }

                if ((i + 1) < lineMembers.length && lineMembers[i + 1].equals("(") &&
                        !isBelongsTo(member, allOperators)) continue;

                if (member.equals("(")) {
                    addOperator("(");
                    if (isMethodHeader)
                        skip = true;
                    continue;
                }

                switch (member) {
                    case "import":
                    case "package":
                        addOperator(";");
                        continue nextLine;
                    case "instanceof":
                        addOperator("instanceof");
                    case "class":
                        skip = true;
                    case ":":
                        continue;
                }

                if (isBelongsTo(member, allOperators)) {
                    addOperator(member);
                } else if (!isBelongsTo(member, otherReservedWords))
                    addOperand(member);
            }
        }

        createAdditions();
    }

    private static String formatCodeLine(String codeLine) {
        /* Проверка наличия содержимого в строке */
        codeLine = codeLine.trim();
        if (codeLine.equals("")) return null;

        /* Уменьшение раздутых пробелов до нормальных размеров */
        while (codeLine.contains("  "))
            codeLine = codeLine.replaceAll("[ ][ ]", " ");

        /* Добавление всех кавычек и содержимого между ними в операнды и их удаление */
        while (codeLine.contains("\"")) {
            int textBegin = codeLine.indexOf("\"");
            int textEnd = codeLine.indexOf("\"", textBegin + 1) + 1;
            addOperand(codeLine.substring(textBegin, textEnd));
            codeLine = codeLine.substring(0, textBegin) +
                    codeLine.substring(textEnd);
        }

        /* Обрамление операторов пробелами */
        int start;
        String prevOperator = "";
        int prevIndex = -1;
        for (String operator : allOperators) {
            start = 0;
            int index;
            while ((index = codeLine.indexOf(operator, start)) != -1) {
                if (!prevOperator.equals("") && codeLine.indexOf(prevOperator, prevIndex) == index) {
                    start = index + prevOperator.length();
                    continue;
                }
                prevOperator = operator;
                prevIndex = index;
                if (index > 0 && codeLine.charAt(index - 1) != ' ' &&
                        index + operator.length() < codeLine.length() - 1 &&
                        codeLine.charAt(index + operator.length()) != ' ')
                    codeLine = codeLine.substring(0, index) + " " +
                            codeLine.substring(index, index + operator.length()) + " " +
                            codeLine.substring(index + operator.length());
                else if (index > 0 && codeLine.charAt(index - 1) != ' ')
                    codeLine = codeLine.substring(0, index) + " "
                            + codeLine.substring(index);
                else if (index + operator.length() < codeLine.length() &&
                        codeLine.charAt(index + operator.length()) != ' ')
                    codeLine = codeLine.substring(0, index + operator.length()) + " " +
                            codeLine.substring(index + operator.length());

                start = index + operator.length() + 1;
            }
        }
        return codeLine;
    }

    private static boolean isMethodHeader(String codeLine) {
        String[] lineMembers = codeLine.split(" ");
        if (lineMembers.length < 3 || !codeLine.contains("(") || !codeLine.contains(")")) return false;
        boolean returnTypeDeclared = false,
                methodNameDeclared = false;
        for (String member : lineMembers) {
            if (!member.equals("(") && isBelongsTo(member, allOperators)) return false;
            if (!isBelongsTo(member, methodHeaderSigns)) {
                if (!returnTypeDeclared)
                    returnTypeDeclared = true;
                else if (!methodNameDeclared)
                    methodNameDeclared = true;
                else return member.equals("(");
            }
        }
        return false;
    }

    private static boolean isBelongsTo(String x, String[] group) {
        for (String groupMember : group)
            if (x.equals(groupMember)) return true;
        return false;
    }
}