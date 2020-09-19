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

        addOperator("&");
        addOperator("&");
        addOperator("$");
        addOperand("a");
        addOperand("a");
        addOperand("b");
        addOperand("a");
        //Put here analyze logic...
        //Arraylists "operators", "operands" must be fulfilled here

        createAdditions();
    }
}
