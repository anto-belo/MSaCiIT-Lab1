package sample.model;

import static sample.controller.MainController.*;
import static sample.model.Operandor.currentOperandNumber;
import static sample.model.Operandor.currentOperatorNumber;

public class Addition {
    private final String key;
    private final double value;

    public Addition(String key, double value) {
        this.key = key;
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public String getKey() {
        return key;
    }

    public static void createAdditions() {
        int programVocabulary = currentOperandNumber + currentOperatorNumber;
        int operatorQuantity = 0;
        for (Operandor operator : operators) operatorQuantity += operator.getQuantity();
        int operandQuantity = 0;
        for (Operandor operand : operands) operandQuantity += operand.getQuantity();
        int programLength = operandQuantity + operatorQuantity;
        double programVolume = programLength * (Math.log(programVocabulary) / Math.log(2));
        programVolume = Math.round(programVolume * 100) / 100.0;

        additions.add(new Addition("Operator's Vocabulary (\u03B71)", currentOperatorNumber));
        additions.add(new Addition("Operand's Vocabulary (\u03B72)", currentOperandNumber));
        additions.add(new Addition("Program Vocabulary (\u03B71 + \u03B72)", programVocabulary));
        additions.add(new Addition("Operator's Quantity (N1)", operatorQuantity));
        additions.add(new Addition("Operand's Quantity (N2)", operandQuantity));
        additions.add(new Addition("Program Length (N1 + N2)", programLength));
        additions.add(new Addition("Program Volume (N * log(2, \u03B7))", programVolume));
    }
}
