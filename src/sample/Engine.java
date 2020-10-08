package sample;

import java.util.ArrayList;

import static sample.controller.MainController.*;
import static sample.model.Addition.createAdditions;
import static sample.model.Operandor.addOperand;
import static sample.model.Operandor.addOperator;

public class Engine {

    private static final String[] allOperators = {"+=", "++", "+", "-=", "--", "-", "*=", "*", "/=", "/", "%=", "%",
            "&=", "&&", "&", "|=", "||", "|", "^=", "^", ">>=", ">>", "<<=", "<<", ">>>", "~", "<=", "<", ">=", ">",
            "!=", "==", "=", ",", ";", "(", ")", "!", "{", "}", "instanceof", "break", "continue", "switch",
            "if", "do", "while", "for", "return", "?", ":", "[", "]"};

    private static final String[] textOperators = {"instanceof", "break", "continue", "switch", "if", "do", "while",
            "for", "return"};

    private static final String[] otherReservedWords = {"byte", "short", "int", "long", "char", "float", "double",
            "boolean", "else", "case", "default", "try", "catch", "finally", "throw", "throws", "private", "protected",
            "public", "import", "package", "class", "interface", "extends", "implements", "static", "final", "void",
            "abstract", "native", "new", "this", "super", "synchronized", "volatile", "const", "goto", "enum", "assert",
            "transient", "strictfp"};

    private static final String[] methodModifiers = {"private", "protected", "public", "static", "final", "abstract",
            "native", "strictfp", "synchronized"};

    private static final String[] fieldModifiers = {"private", "protected", "public", "static", "final", "volatile",
            "transient"};

    private static boolean inComment; // находимся ли внутри многострочного комментария
    private static boolean implementationDeclarations; // находимся ли на стадии implements
    private static boolean doCycle; // находимся ли в цикле do..while
    private static boolean forCycle;
    private static boolean skippingMethod; // пропускаем ли в этот момент неиспользуемый метод
    private static int bracketCount; // щытаем скобочки
    private static String className; // имя класса

    public static void analyze() {
        operators = new ArrayList<>();
        operands = new ArrayList<>();
        additions = new ArrayList<>();

        nextLine:
        for (String codeLine : code) {
            if (!skippingMethod)
            codeLine = formatCodeLine(codeLine); // наводим красоту в строке
            if (codeLine == null) continue;

            String methodName;
            boolean header = (methodName = isMethodHeaderBeginning(codeLine)) != null; // пробуем идентифицировать строку как заголовок функции
            boolean field = !header && isClassField(codeLine); // пробуем идентифицировать строку как поле класса или метода

            String[] lineMembers = codeLine.split(" ");
            boolean skip = false; // для пропуска слова
            boolean typeDeclared = false; // если строка идентифицирована как поле
            /* делим строку на слова и начинаем последовательную проверку */
            for (int i = 0; i < lineMembers.length; i++) {
                if (skip) { // пропуск слова
                    skip = false;
                    continue;
                }

                String member = lineMembers[i];

                /* если находимся на стадии пропуска метода, то считаем скобочки на каждой строке, пока
                 * сумма не будет равна нулю. Как только ноль - переходим либо к следующему слову после }, либо
                 * на новую строку, если на предыдущей конец*/
                if (skippingMethod) {
                    if (codeLine.contains("{")) bracketCount++;
                    if (codeLine.contains("}")) bracketCount--;
                    if (bracketCount == 0) {
                        skippingMethod = false;
                        int indexOfLastBracket = codeLine.indexOf("}");
                        if (indexOfLastBracket + 1 < codeLine.length())
                            member = lineMembers[indexOfLastBracket + 1];
                        else continue nextLine;
                    } else continue nextLine;
                }

                /* опять пытаемся идентифицировать строку как заголовок/поле, только теперь передаем
                 * не всю строку, а подстроку, начиная с текущего слова*/
                int indexFrom = codeLine.indexOf(" " + member + " ") + 1;
                indexFrom = (indexFrom == 0) ? codeLine.indexOf(" " + member) + 1 : indexFrom;
                String substring = codeLine.substring(indexFrom);
                if (!header) header = (methodName = isMethodHeaderBeginning(substring)) != null;
                if (!header && !field) field = isClassField(substring);

                /* определяем, используется ли метод в коде. Если нет - начинаем процесс его пропуска */
                if (header && isUnusedMethod(methodName) && !skippingMethod &&
                        !methodName.equals("main") && !methodName.equals("analyze")) {
                    skippingMethod = true;
                    if (codeLine.contains("{")) bracketCount++;
                    if (codeLine.contains("}")) bracketCount--;
                    continue nextLine;
                }

                /* Полукостыльная штука для полей конструктора типа this.field = field; */
                if (member.startsWith("this") && member.contains(".")) {
                    addOperand("this");
                    member = member.split("\\.", 2)[1];
                }

                /* Почти такая же штука, но для енума (Color c = Color.A;) */
                if (member.contains(".") && !member.matches("[0-9]+[.][0-9]+")) {
                    String[] submembers = member.split("\\.");
                    member = submembers[submembers.length - 1];
                }

                /* Не учитываем слово while, если в do..while цикле */
                if (doCycle && member.equals("while")) {
                    doCycle = false;
                    continue;
                }

                if (forCycle && member.equals("(")) {
                    forCycle = false;
                    skip = true;
                }

                if (member.equals("new") && i + 2 < codeLine.length() && lineMembers[i + 2].equals("(")) {
                    continue;
                } else if (member.equals("new")) {
                    skip = true;
                    continue;
                }

                /* В заголовке класса: имена интерфейсов пропускаем, запятые добавляем */
                if (member.equals(",") && implementationDeclarations) {
                    addOperator(",");
                    skip = true;
                    continue;
                }

                /* конец декларации интерфейсов => хватит игнорировать слова после запятых */
                if (member.equals("{") && implementationDeclarations) {
                    implementationDeclarations = false;
                }

                /* если после текущего слова идет (, значит это - функция => добавляем ее в операторы */
                if ((i + 1) < lineMembers.length && lineMembers[i + 1].equals("(") &&
                        !isBelongsTo(member, allOperators)) {
                    addOperator(member + "()");
                    continue;
                }

                /* Если текущее слово - (, значит началось что связанное с функцией. Если строка - заголовок, то
                 * пропускаем первое слово после скобки (это буит тип операнда) */
                if (member.equals("(")) {
                    addOperator("(");
                    if (header && i + 1 < lineMembers.length && !lineMembers[i + 1].equals(")"))
                        skip = true;
                    continue;
                }

                if (member.equals(",") && header)
                    skip = true;

                /* Много разной всяки */
                switch (member) {
                    /* на строках с импортом и пэкэджем нужна только ; */
                    case "import":
                    case "package":
                        addOperator(";");
                        continue nextLine;
                        /* игнор слова после instanceof*/
                    case "instanceof":
                        addOperator("instanceof");
                        skip = true;
                        continue;
                        /* активация распознавания имен интерфейсов */
                    case "implements":
                        implementationDeclarations = true;
                        skip = true;
                        continue;
                        /* следующее после class слово - имя класса */
                    case "class":
                        className = lineMembers[i + 1];
                        /* пропускаем имя енума или расширяемого класса */
                    case "extends":
                    case "enum":
                        skip = true;
                        continue;
                        /* просто добавляем ассерт */
                    case "assert":
                        addOperator("assert");
                        continue;
                        /* игнорим new и : */
                    case "->":
                        addOperator("->");
                    case ":":
                    case "[":
                    case "]":
                        continue;
                        /* мы у do..while цикле */
                    case "do":
                        doCycle = true;
                    case "for":
                        forCycle = true;
                    case ";":
                        field = false;
                }

                /* логично */
                if (isBelongsTo(member, allOperators))
                    addOperator(member);
                    /* на строках-полях первое слово после модификов - тип, пропускаем его */
                else if (field && !isBelongsTo(member, fieldModifiers) && !typeDeclared)
                    typeDeclared = true;
                    /* ну тут уже ничего не остается */
                else if (!isBelongsTo(member, otherReservedWords))
                    addOperand(member);
            }
        }

        createAdditions();
    }

    /* наводилка красоты на строчке */
    private static String formatCodeLine(String codeLine) {
        codeLine = codeLine.trim();

        /* Однострочные комментарии */
        int commentStart;
        if ((commentStart = codeLine.indexOf("//")) != -1)
            codeLine = codeLine.substring(0, commentStart);

        /* Многострочные комментарии */
        int commentEnd;
        if (inComment && !codeLine.contains("*/"))
            return null;
        else if (inComment && (commentEnd = codeLine.indexOf("*/")) != -1) {
            codeLine = codeLine.substring(commentEnd + 2);
            inComment = false;
        }
        if ((commentStart = codeLine.indexOf("/*")) != -1 && (commentEnd = codeLine.indexOf("*/")) != -1) {
            if (commentEnd + 2 < codeLine.length())
                codeLine = codeLine.substring(0, commentStart) + codeLine.substring(commentEnd + 2);
            else codeLine = codeLine.substring(0, commentStart);
        } else if ((commentStart = codeLine.indexOf("/*")) != -1) {
            codeLine = codeLine.substring(0, commentStart);
            inComment = true;
        }

        if (codeLine.equals("")) return null;

        /* Уменьшение раздутых пробелов до нормальных размеров */
        while (codeLine.contains("  "))
            codeLine = codeLine.replaceAll("[ ][ ]", " ");

        /* Добавление всех кавычек и содержимого между ними в операнды и их удаление */
        while (codeLine.contains("\"")) {
            int textBegin = codeLine.indexOf("\"");
            int textEnd = codeLine.indexOf("\"", textBegin + 1) + 1;
            if (textEnd == 0) break;
            if (textEnd < codeLine.length() && codeLine.charAt(textEnd) == '"') textEnd++;
            addOperand(codeLine.substring(textBegin, textEnd));
            codeLine = codeLine.substring(0, textBegin) +
                    codeLine.substring(textEnd);
        }

        /* тоже самое для одинарных кавычек */
        while (codeLine.contains("'")) {
            int symbolBegin = codeLine.indexOf("'");
            int symbolEnd = symbolBegin + 3;
            if (codeLine.charAt(symbolEnd - 1) != '\'') symbolEnd++;
            if (codeLine.charAt(symbolEnd) == '\'') symbolEnd++;
            addOperand(codeLine.substring(symbolBegin, symbolEnd));
            codeLine = codeLine.substring(0, symbolBegin) +
                    codeLine.substring(symbolEnd);
        }

        /* опять удаляем появившиеся большие пробелы */
        while (codeLine.contains("  "))
            codeLine = codeLine.replaceAll("[ ][ ]", " ");

        /* Обрамление операторов пробелами */
        int start;
        String prevOperator = "";
        int curIndex = -1;
        for (int i = 0; i < allOperators.length; i++) {
            String operator = allOperators[i];
            start = 0;
            int index;
            outer:
            while ((index = codeLine.indexOf(operator, start)) != -1) {
                /* это, например, чтобы do не совпадал c double, doc и тд*/
                if (isBelongsTo(operator, textOperators)) {
                    char nextLetter = (index + operator.length() < codeLine.length()) ?
                            codeLine.charAt(index + operator.length()) : '\0';
                    if (('a' < nextLetter && nextLetter < 'z') || ('A' < nextLetter && nextLetter < 'Z') ||
                            nextLetter == '$' || nextLetter == '_') break;
                }

                /* это чтобы не зациклиться на одном вхождении оператора в строку */
                int prevIndex = codeLine.indexOf(prevOperator, curIndex);
                if (!prevOperator.equals("") && prevIndex <= index && index <= prevIndex + operator.length()) {
                    start = index + prevOperator.length();
                    continue;
                }

                if (operator.equals("[") && i + 1 < allOperators.length && codeLine.charAt(index + 1) == ']') {
                    start = index + 2;
                    continue;
                } else if (operator.equals("]") && i > 0 && codeLine.charAt(index - 1) == '[') {
                    start = index + 1;
                    continue;
                }

                /* составные операторы, поэтому проверим все комбинации, чтобы, например, какой-нибудь
                 * + или = не совпали с += */
                String[] partOps = {"+", "-", "*", "/", "%", "<", ">", "|", "&", "^", ">>", "<<", "!", "="};
                if ((isBelongsTo(operator, partOps)) && index > 0 && index < codeLine.length() - 1) {
                    for (String op : partOps)
                        if (String.valueOf(codeLine.charAt(index - 1)).equals(op) ||
                                String.valueOf(codeLine.charAt(index + 1)).equals(op)) {
                            start = index + op.length();
                            continue outer;
                        }
                }

                prevOperator = operator;
                curIndex = index;
                /* ab(c -> ab ( c */
                if (index > 0 && codeLine.charAt(index - 1) != ' ' &&
                        index + operator.length() < codeLine.length() &&
                        codeLine.charAt(index + operator.length()) != ' ')
                    codeLine = codeLine.substring(0, index) + " " +
                            codeLine.substring(index, index + operator.length()) + " " +
                            codeLine.substring(index + operator.length());
                    /* (abc -> ( abc */
                else if (index > 0 && codeLine.charAt(index - 1) != ' ')
                    codeLine = codeLine.substring(0, index) + " "
                            + codeLine.substring(index);
                    /* abc) -> abc ) */
                else if (index + operator.length() < codeLine.length() &&
                        codeLine.charAt(index + operator.length()) != ' ')
                    codeLine = codeLine.substring(0, index + operator.length()) + " " +
                            codeLine.substring(index + operator.length());

                /* опять-таки, чтобы не зациклиться */
                start = index + operator.length() + 1;
            }
        }
        return codeLine;
    }

    /* пытаемся понять, является ли начало строки заголовком функции */
    private static String isMethodHeaderBeginning(String codeLine) {
        String[] lineMembers = codeLine.split(" ");
        /* базовые требования */
        if (lineMembers.length < 3 || !codeLine.contains("(") || !codeLine.contains(")")) return null;
        boolean returnTypeDeclared = false,
                methodNameDeclared = false;
        String methodName = "";
        for (String member : lineMembers) {
            /* слово в строке может быть ( только если объявлено имя конструктора (флаг returnType (ну потому что
            сдвиг на слово ибо у конструктора нет возвращаемого типа))*/
            if ((!member.equals("(") && isBelongsTo(member, allOperators)) ||
                    (!returnTypeDeclared && member.equals("(")) || member.equals("new")) break;
            /* начинаем анализировать, когда заканчиваются модификаторы */
            if (!isBelongsTo(member, methodModifiers)) {
                /* потенциально слово - это имя */
                if (!isBelongsTo(member, allOperators)) methodName = member;
                /* логично */
                if (!returnTypeDeclared)
                    returnTypeDeclared = true;
                    /* проверка на конструктор */
                else if (!methodNameDeclared && member.equals("(") && methodName.equals(className))
                    return methodName;
                    /* логично */
                else if (!methodNameDeclared)
                    methodNameDeclared = true;
                    /* логично */
                else if (member.equals("("))
                    return methodName;
                else return null;
            }
        }
        return null;
    }

    /* опять определяем, но уже является ли полем начало строки (объяснение +- такое же как и для заголовка) */
    private static boolean isClassField(String codeLine) {
        if (codeLine.startsWith("=")) return false;
        String[] lineMembers = codeLine.split(" ");
        boolean typeDeclared = false,
                nameDeclared = false;
        for (String member : lineMembers) {
            if (!member.equals("=") && !member.equals(";") && isBelongsTo(member, allOperators)) break;
            if (!isBelongsTo(member, fieldModifiers)) {
                if (!typeDeclared)
                    typeDeclared = true;
                else if (!nameDeclared)
                    nameDeclared = true;
                else return member.equals("=") || member.equals(";");
            }
        }
        return false;
    }

    /* проверяем, используется ли метод в коде */
    private static boolean isUnusedMethod(String name) {
        int count = 0;
        for (String line : code) {
            if (line.contains("class") || line.contains("package") ||
                    line.contains("import")) continue;
            if (line.contains(name)) count++;
        }
        return count == 1;
    }

    /* всё логично */
    private static boolean isBelongsTo(String x, String[] group) {
        for (String groupMember : group)
            if (x.equals(groupMember)) return true;
        return false;
    }
}