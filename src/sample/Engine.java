package sample;

import java.util.ArrayList;

import static sample.controller.MainController.*;
import static sample.model.Addition.createAdditions;
import static sample.model.Operandor.addOperand;
import static sample.model.Operandor.addOperator;

public class Engine {

    private static final String[] allOperators = {"+=", "++", "+", "-=", "--", "-", "*=", "*", "/=", "/", "%=", "%",
            "&=", "&&", "&", "|=", "||", "|", "^=", "^", ">>=", ">>", "<<=", "<<", ">>>", "~", "<=", "<", ">=", ">",
            "!=", "==", "=", ".", ",", ";", "(", ")", "!", "{", "}", "instanceof", "break", "continue", "switch",
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
        nextLine:
        for (String codeLine : code) {
            /* Проверка наличия содержимого строки */
            System.out.println("Line " + lineNo++ + ":");
            codeLine = codeLine.trim();
            if (codeLine.equals("")) continue;


            while (codeLine.contains("  "))
                codeLine = codeLine.replaceAll("[ ][ ]", " ");

            for (String word : codeLine.split(" ")) {
                int start = 0;
                String prevOperator = "";
                for (String operator : allOperators) {
                    if (prevOperator.contains(operator)) start += prevOperator.length();
                    int index = word.indexOf(operator, start);
                    if (index == -1) {
                        start = 0;
                        continue;
                    }
                    prevOperator = operator;
                    String oldWord = word;
                    if (word.startsWith(operator)) word = word.substring(0, operator.length()) + " "
                                         + word.substring(operator.length());
                    else if (word.endsWith(operator)) word = word.substring(0, index) + " "
                                         + word.substring(index);
                    //if (!word.startsWith(operator) && !word.endsWith(operator))
                    else word = word.substring(0, index) + " "
                              + word.substring(index, index + operator.length()) + " "
                              + word.substring(index + operator.length());
                    start = index + operator.length();
                    codeLine = codeLine.replace(oldWord, word);
                }
            }

            /* Удаление всех кавычек и содержимого между ними */
            while (codeLine.contains("\"")) {
                int textBegin = codeLine.indexOf("\"");
                int textEnd = codeLine.indexOf("\"", textBegin + 1) + 1;
                codeLine = codeLine.substring(0, textBegin) +
                        codeLine.substring(textEnd);
            }

            /* Распознавание заголовка функции или её вызова */
            boolean header, call = false;
            if ((header = isMethodHeader(codeLine)) || (call = isMethodCall(codeLine))) {
                getMethodArgs(codeLine);
                if (header) addOperator("{");
                if (call) addOperator(";");
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
                        addOperator(";");
                        continue nextLine;
                    case "instanceof":
                        addOperator("instanceof");
//                        skip = true;
//                        continue;
                    case "class":
                        skip = true;
                    case ":":
                        continue;
                }

                /* Общий случай */
                String operator;
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
                    if (member.endsWith(";") || member.endsWith(","))
                        member = member.substring(0, member.length() - 1);

                    for (String post : new String[]{"++", "--"})
                        if (member.endsWith(post)) {
                            addOperator(post);
                            member = member.substring(0, member.length() - 2);
                        }

                    for (String pre : new String[]{"++", "--"})
                        if (member.startsWith(pre)) {
                            addOperator(pre);
                            member = member.substring(2);
                        }

//                    if (member.endsWith("++")) {
//                        addOperator("++");
//                        member = member.substring(0, member.length() - 2);
//                    } else if (member.endsWith("--")) {
//                        addOperator("--");
//                        member = member.substring(0, member.length() - 2);
//                    } else if (member.startsWith("++")) {
//                        addOperator("++");
//                        member = member.substring(2);
//                    } else if (member.startsWith("--")) {
//                        addOperator("--");
//                        member = member.substring(2);
//                    }


                    getFuncInFuncArgs(member);

//                    if (member.startsWith("(")) member = member.substring(1);
//                    else member = member.substring(0, member.length() - 1);
                }
                if (member.endsWith(":")) member = member.substring(0, member.length() - 1);
                if (isBelongsTo(member, allOperators))
                    addOperator(member);
                else if (!isBelongsTo(member, otherReservedWords))
                    addOperand(member);
            }
        }


        createAdditions();
    }

    private static void getFuncInFuncArgs(String member) {
        if (!member.startsWith("(") && !member.endsWith(")") && member.contains("(")) {
            String[] subMembers = member.split("\\(");
            int operandIndex = subMembers.length - 1;
            //for (int i = 0; i < subMembers.length; i++)


        }
    }

    private static String tryToFindOperator(String member) {
        for (String operator : new String[]{";", ",", ")"})
            if (member.endsWith(operator))
                return operator;
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
        boolean methodNameDeclared = false;
        if (!codeLine.contains("(") || !codeLine.contains(")") || codeLine.contains(" = ")) return false;
        for (String word : codeLine.split(" ")) {
            if (word.equals("if") || word.equals("for") || word.equals("switch")) return false;
            if (word.contains("(") || word.endsWith("()")) methodNameDeclared = true;
            if (isBelongsTo(word, methodHeaderSigns)) return false;
        }
        return methodNameDeclared;
    }

    private static void getMethodArgs(String codeLine) {
        String[] args = codeLine.substring(codeLine.indexOf('(') + 1, codeLine.lastIndexOf(')')).split(" ");
        addOperator("(");
        addOperator(")");
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