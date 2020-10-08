package sample.model;

import static sample.controller.MainController.operands;
import static sample.controller.MainController.operators;

public class Operandor {

    private final int num;
    private final String name;
    private int quantity;

    public static int currentOperandNumber;
    public static int currentOperatorNumber;

    public Operandor(int num, String name, int quantity) {
        this.num = num;
        this.name = name;
        this.quantity = quantity;
    }

    public int getNum() {
        return num;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public static void addOperator(String name) {
        if (name.equals("(")) name = "(...)";
        if (name.equals("{")) name = "{...}";
        if (name.equals("switch")) name = "switch..case";
        if (name.equals("if")) name = "if..else";
        if (name.equals("do")) name = "do..while";
        if (name.equals("?")) name = "?..:";

        if (name.equals(")") || name.equals("}")) return;
        for (Operandor operator : operators)
            if (name.equals(operator.name)) {
                operator.quantity++;
                return;
            }
        operators.add(new Operandor(++currentOperatorNumber, name, 1));
    }

    public static void addOperand(String name) {
        for (Operandor operand : operands)
            if (name.equals(operand.name)) {
                operand.quantity++;
                return;
            }
        operands.add(new Operandor(++currentOperandNumber, name, 1));
    }
}
