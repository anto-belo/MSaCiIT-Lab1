package sample;

import java.util.ArrayList;

import static sample.controller.MainController.*;
import static sample.model.Addition.createAdditions;
import static sample.model.Operandor.addOperand;
import static sample.model.Operandor.addOperator;

public class Engine {
    public static void analyze() {
        operators = new ArrayList<>();
        operands = new ArrayList<>();
        additions = new ArrayList<>();



        // Put here analyze logic. Input code is in the Arraylist<String> code
        // Arraylists "operators", "operands" must be fulfilled here by next methods:
        // addOperator(String operatorName);
        // addOperand(String operandName).
        // For example:
        addOperator("&");
        addOperand("a");



        createAdditions();
    }
}
