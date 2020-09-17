package sample.model;

public class Operandor {

    private int num;
    private String name;
    private int quantity;
    private static int currentOperandNumber;
    private static int currentOperatorNumber;

    public Operandor(int num, String name, int quantity) {
        this.num = num;
        this.name = name;
        this.quantity = quantity;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
