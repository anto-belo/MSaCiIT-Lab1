package sample.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import sample.model.Additional;
import sample.model.Operandor;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import static sample.Main.primaryStage;

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
    private TableView<Additional> additionalInfoTable;

    @FXML
    private TableColumn<Additional, String> keyInfoColumn;

    @FXML
    private TableColumn<Additional, String> valueInfoColumn;

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

    private int metricsPointer;

    private ArrayList<String> code;

    private ArrayList<Operandor> operators;

    private ArrayList<Operandor> operands;

    private ArrayList<Additional> additions;

    @FXML
    void initialize() {

        analyzeButton.setOnAction(actionEvent -> {
            fillOperatorsTable();
            fillOperandsTable();
            fillAdditionalTable();

            operatorsTable.setVisible(true);
            operandsTable.setVisible(false);
            additionalInfoTable.setVisible(false);
            metricsPane.setVisible(true);
            metricsPointer = 0;
        });

        loadButton.setOnAction(actionEvent -> {
            try {
                loadCodeFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        clearButton.setOnAction(actionEvent -> {
            codeInput.setText("");
            metricsPane.setVisible(false);
        });

        prevButton.setOnAction(actionEvent -> turnMetricsTable(false));

        nextButton.setOnAction(actionEvent -> turnMetricsTable(true));
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
                additionalInfoTable.setVisible(false);
                break;
            }
            case 1: {
                operatorsTable.setVisible(false);
                operandsTable.setVisible(true);
                additionalInfoTable.setVisible(false);
                break;
            }
            case 2: {
                operatorsTable.setVisible(false);
                operandsTable.setVisible(false);
                additionalInfoTable.setVisible(true);
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

    private void loadCodeFile() throws IOException {
        File file = getCodeFile();
        if (file == null) return;

        getCodeList(file);

        for (String line : code)
            codeInput.appendText(line + "\n");
    }

    private void fillAdditionalTable() {
        additionalInfoTable.setItems(getObservableList(additions));
        keyInfoColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
        valueInfoColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
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
        /*for (int i = 0; i < code.size(); i++)
            observableList.add(i,code.get(i));*/
        return observableList;
    }
}
