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

            /* Распознавание заголовка функции */
            /*if (isMethodHeader(codeLine)) {
                getHeaderArgs(codeLine);
                addOperator("{");
                continue;
            }*/
            boolean isMethodHeader = isMethodHeader(codeLine);

            /* Распознавание отдельных слов */
            boolean skip = false;
            String[] lineMembers = codeLine.split(" ");
            for (int i = 0; i < lineMembers.length; i++) {
                String member = lineMembers[i];
                if (member.equals("")) continue;
                if (!isMethodHeader) isMethodHeader = isMethodHeader(codeLine.substring(codeLine.indexOf(member)));

                /* Пропуск слова после instanceof или class */
                if (skip) {
                    skip = false;
                    continue;
                }

                /* Пропуск имени метода */
                if ((i + 1) < lineMembers.length && lineMembers[i + 1].equals("(") &&
                    !isBelongsTo(member, allOperators)) continue;

                if (member.equals("(")) {
                    addOperator("(");
                    if (isMethodHeader)
                        skip = true;
                    continue;
                }

                /* --- Частные случаи --- */
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

                /* --- Общий случай --- */
                String operator;
                /* Проверка на оператор */
                if (isBelongsTo(member, allOperators)) {
                    addOperator(member);
                } //else if (isMethodCall(codeLine.substring(codeLine.indexOf(member))) > 0) {}
                /* //2-я попытка найти оператор в слове
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

                    for (String op : new String[]{"++", "--"})
                        if (member.endsWith(op)) {
                            addOperator(op);
                            member = member.substring(0, member.length() - 2);
                        } else if (member.startsWith(op)) {
                            addOperator(op);
                            member = member.substring(2);
                        }
                }
                if (member.endsWith(":")) member = member.substring(0, member.length() - 1);
                if (isBelongsTo(member, allOperators))
                    addOperator(member);*/
                else if (!isBelongsTo(member, otherReservedWords))
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

               /* if (codeLine.startsWith(operator) && codeLine.charAt(operator.length()) != ' ')
                    codeLine = codeLine.substring(0, operator.length()) + " "
                            + codeLine.substring(operator.length());
                else if (codeLine.endsWith(operator) && codeLine.charAt(codeLine.length() - 1 - operator.length()) != ' ')
                    codeLine = codeLine.substring(0, index) + " "
                            + codeLine.substring(index);
                else codeLine = codeLine.substring(0, index) + " "
                            + codeLine.substring(index, index + operator.length()) + " "
                            + codeLine.substring(index + operator.length());*/
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

    /*private static boolean isMethodHeader(String codeLine) {
        String[] lineMembers = codeLine.split(" ");
        if (lineMembers.length < 3 || !codeLine.contains("(") || !codeLine.contains(")")) return false;
        boolean returnTypeDeclared = false,
                methodNameDeclared = false,
                paramsOpen = false,
                operandTypeDeclared = false,
                operandDeclared = false,
                methodOpened = false;
        for (String lineMember : lineMembers) {
            if (!isBelongsTo(lineMember, methodHeaderSigns)) {
                if (!returnTypeDeclared)
                    returnTypeDeclared = true;
                else if (!methodNameDeclared)
                    methodNameDeclared = true;
                else if (lineMember.equals("(") && !paramsOpen)
                    paramsOpen = true;
                else if (paramsOpen && !operandTypeDeclared)
                    operandTypeDeclared = true;
                else if (operandTypeDeclared && !operandDeclared)
                    operandDeclared = true;
                else if (lineMember.equals(","))
                    operandTypeDeclared = operandDeclared = false;
                else if (lineMember.equals(")") && paramsOpen)
                    paramsOpen = false;
                else if (!methodOpened && !paramsOpen && lineMember.equals("{"))
                    methodOpened = true;
                else return false;
            }
        }
        return true;
    }*/

    /*private static void getHeaderArgs(String codeLine) {

    }

    private static int isMethodCall(String codeLine) {
        String[] members = codeLine.split(" ");
        if (members.length < 3 ||
            isBelongsTo(members[0], otherReservedWords) ||
            isBelongsTo(members[0], allOperators) ||
            members[0].equals("if") ||
            members[0].equals("for") ||
            members[0].equals("switch") ||
            !members[1].equals("(") ||
            !codeLine.contains(")")) return -1;
        int bracketCount = 1;
        boolean operandDeclared = false;
        for (int i = 2; i < members.length; i++) {
            String member = members[i];
            int j = isMethodCall(codeLine.substring(codeLine.indexOf(member)));
            if(j > 0)
                i += j;
            else if (j == 0)
                return 0;
            else if (member.equals(")"))
                bracketCount--;
            else if (!operandDeclared)
                operandDeclared = true;

            if (bracketCount == 0 && (i + 1) < members.length)
                return codeLine.indexOf(members[i + 1]);
            else if (bracketCount == 0) return 0;
        }
        return -1;
    }*/

    /*private static boolean isMethodCall(String codeLine) {
        if (!codeLine.contains("(") || !codeLine.contains(")")) return false;
        boolean methodNameDeclared = false,
                paramsOpen = false,
                operandDeclared = false;
        for (String word : codeLine.split(" ")) {
            if (word.equals("if") || word.equals("for") || word.equals("switch") ||
                    isBelongsTo(word, methodHeaderSigns)) return false;
            if (!methodNameDeclared && word.equals("(")) return false;
            else methodNameDeclared = true;

            if (word.equals("(") && !paramsOpen)
                paramsOpen = true;
            else if (paramsOpen && !operandDeclared)
                operandDeclared = true;
            else if (word.equals(","))
                operandDeclared = false;
            else if (word.equals(")") && paramsOpen)
                paramsOpen = false;
        }
        return methodNameDeclared;
    }*/

    /*private static void getCallArgs(String codeLine) {
        String[] args = codeLine.substring(codeLine.indexOf('(') + 1, codeLine.lastIndexOf(')')).split(" ");
        addOperator("(");
        addOperator(")");
        for (int i = 1; i < args.length; i += 3) {
            if (i < args.length - 1 && args[i + 1].equals("(")) {
                int bracketCount = 1;
                int j = i + 2;
                if (j >= args.length) return;
                StringBuilder innerMethodArgsLine = new StringBuilder("( ");
                while (bracketCount != 0 || j != args.length) {
                    if (args[j].equals("(")) bracketCount++;
                    else if (args[j].equals(")")) bracketCount--;
                    innerMethodArgsLine.append(args[j]).append(" ");
                    j++;
                }
                innerMethodArgsLine.append(")");
                getCallArgs(innerMethodArgsLine.toString());
                i = j;
            } else addOperand(args[i]);
        }
    }*/

    /*private static String tryToFindOperator(String member) { //todo useless..?
        for (String operator : new String[]{";", ",", ")"})
            if (member.endsWith(operator))
                return operator;
        if (member.startsWith("(")) return "(";
        if (member.endsWith(":"))
            member = member.substring(0, member.length() - 1);
        if (isBelongsTo(member, allOperators)) return member;
        else return null;
    }*/

    /*private static void getFuncInFuncArgs(String member) {
        if (!member.startsWith("(") && !member.endsWith(")") && member.contains("(")) {
            String[] subMembers = member.split("\\(");
            int operandIndex = subMembers.length - 1;
            //for (int i = 0; i < subMembers.length; i++)
        }
    }*/

    private static boolean isBelongsTo(String x, String[] group) {
        for (String groupMember : group)
            if (x.equals(groupMember)) return true;
        return false;
    }
}