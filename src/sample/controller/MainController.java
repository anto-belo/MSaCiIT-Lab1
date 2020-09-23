package sample.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import sample.model.Addition;
import sample.model.Operandor;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import static sample.Engine.analyze;
import static sample.Main.primaryStage;
import static sample.model.Operandor.currentOperandNumber;
import static sample.model.Operandor.currentOperatorNumber;

public class MainController {

    @FXML
    private AnchorPane metricsPane;

    @FXML
    private TableView<Operandor> operatorsTable;

    @FXML
    private TableColumn<Operandor, String> numOperatorColumn;

    @FXML
    private TableColumn<Operandor, String> nameOperatorColumn;

    @FXML
    private TableColumn<Operandor, String> quantityOperatorColumn;

    @FXML
    private TableView<Operandor> operandsTable;

    @FXML
    private TableColumn<Operandor, String> numOperandColumn;

    @FXML
    private TableColumn<Operandor, String> nameOperandColumn;

    @FXML
    private TableColumn<Operandor, String> quantityOperandColumn;

    @FXML
    private TableView<Addition> additionsTable;

    @FXML
    private TableColumn<Addition, String> keyAdditionColumn;

    @FXML
    private TableColumn<Addition, String> valueAdditionColumn;

    @FXML
    private Button prevButton;

    @FXML
    private Button nextButton;

    @FXML
    private TextArea codeInput;

    @FXML
    private Button analyzeButton;

    @FXML
    private Button clearButton;

    @FXML
    private Button loadButton;

    @FXML
    private Button warningNote;

    private int metricsPointer;

    public static ArrayList<String> code;

    public static ArrayList<Operandor> operators;

    public static ArrayList<Operandor> operands;

    public static ArrayList<Addition> additions;

    @FXML
    void initialize() {

        analyzeButton.setOnAction(actionEvent -> {
            currentOperatorNumber = currentOperandNumber = 0;
            getCodeList();
            if (code.size() == 1 && code.get(0).equals("")) {
                metricsPane.setVisible(false);
                showWarning();
                return;
            }
            analyze();
            showAnalysis();
        });

        loadButton.setOnAction(actionEvent -> {
            try {
                loadCodeFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        clearButton.setOnAction(actionEvent -> clearWindow());

        prevButton.setOnAction(actionEvent -> turnMetricsTable(false));

        nextButton.setOnAction(actionEvent -> turnMetricsTable(true));
    }

    private void showAnalysis() {
        fillOperatorsTable();
        fillOperandsTable();
        fillAdditionalTable();

        operatorsTable.setVisible(true);
        operandsTable.setVisible(false);
        additionsTable.setVisible(false);
        metricsPane.setVisible(true);
        metricsPointer = 0;
    }

    private void clearWindow() {
        code = null;
        operators = null;
        operands = null;
        additions = null;

        codeInput.setText("");
        metricsPane.setVisible(false);
    }

    private void turnMetricsTable(boolean dest) {
        if (dest) metricsPointer++;
        else metricsPointer--;

        metricsPointer = (metricsPointer == 3) ? 0 : metricsPointer;
        metricsPointer = (metricsPointer == -1) ? 2 : metricsPointer;

        switch (metricsPointer) {
            case 0: {
                operatorsTable.setVisible(true);
                operandsTable.setVisible(false);
                additionsTable.setVisible(false);
                break;
            }
            case 1: {
                operatorsTable.setVisible(false);
                operandsTable.setVisible(true);
                additionsTable.setVisible(false);
                break;
            }
            case 2: {
                operatorsTable.setVisible(false);
                operandsTable.setVisible(false);
                additionsTable.setVisible(true);
                break;
            }
        }
    }

    private File getCodeFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Java Code File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text File", "*.txt"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        return fileChooser.showOpenDialog(primaryStage);
    }

    private void getCodeList(File file) throws IOException {
        FileReader fr = new FileReader(file);
        Scanner sc = new Scanner(fr);
        code = new ArrayList<>();

        while (sc.hasNextLine())
            code.add(sc.nextLine());

        sc.close();
        fr.close();
    }

    private void getCodeList() {
        code = new ArrayList<>(Arrays.asList(codeInput.getText().split("\n")));
    }

    private void loadCodeFile() throws IOException {
        File file = getCodeFile();
        if (file == null) return;

        getCodeList(file);

        codeInput.clear();
        for (String line : code)
            codeInput.appendText(line + "\n");
    }

    private void fillAdditionalTable() {
        additionsTable.setItems(getObservableList(additions));
        keyAdditionColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
        valueAdditionColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
    }

    private void fillOperatorsTable() {
        operatorsTable.setItems(getObservableList(operators));
        numOperatorColumn.setCellValueFactory(new PropertyValueFactory<>("num"));
        nameOperatorColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        quantityOperatorColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
    }

    private void fillOperandsTable() {
        operandsTable.setItems(getObservableList(operands));
        numOperandColumn.setCellValueFactory(new PropertyValueFactory<>("num"));
        nameOperandColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        quantityOperandColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
    }

    private <E> ObservableList<E> getObservableList(ArrayList<E> list) {
        ObservableList<E> observableList = FXCollections.observableArrayList();
        observableList.setAll(list);
        return observableList;
    }

    private void showWarning() {
        warningNote.setVisible(true);
        Timeline delay = new Timeline(new KeyFrame(Duration.seconds(2), actionEvent -> warningNote.setVisible(false)));
        delay.play();
    }
}
