package pt.isec.pd.trabalho_pratico.ui_controller.Home.Expense;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import pt.isec.pd.trabalho_pratico.Client.Client;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class ExpenseController {
    public Button btnDone;
    @FXML
    private TextField tfDescription;
    @FXML
    private TextField tfAmount;
    @FXML
    private DatePicker datePicker;
    @FXML
    private ComboBox<String> cbGroup;
    @FXML
    private ComboBox<String> cbPaidBy;
    @FXML
    private ScrollPane scrollpaneCheckbox;
    @FXML
    private Label lbSelected;
    @FXML
    private Label lberror;
    @FXML
    private VBox vbCheckboxs;
    private Client client;

    public void initialize() {
        client = Client.getInstance();
        amountFormat();

        lberror.setVisible(false);
        scrollpaneCheckbox.setVisible(false);
        lbSelected.setText("Select options");
        cbGroup.setValue("Choose a group");

        ObservableList<CheckBox> nomeCheckbox =  FXCollections.observableArrayList();
        String [] grupos = client.getNomesGrupoPertence().split(",");
        Set<String> nomesSet = new HashSet<>();

        for (String grupo : grupos)  cbGroup.getItems().add(grupo);

        cbGroup.setOnAction(event -> {
            if (cbGroup.getValue() != null && !cbGroup.getValue().equals("Choose a group")) {
                nomesSet.clear();
                vbCheckboxs.getChildren().clear();
                cbPaidBy.getItems().clear();
                nomeCheckbox.clear();

                String[] nomes = client.getNomesPesoasGrupos(cbGroup.getValue()).split(",");
                Collections.addAll(nomesSet, nomes);

                CheckBox c = new CheckBox("Equally");
                nomeCheckbox.add(c);
                for (String nome : nomesSet) {
                    CheckBox aux = new CheckBox(nome);
                    nomeCheckbox.add(aux);
                    cbPaidBy.getItems().add(nome);
                }
                vbCheckboxs.getChildren().addAll(nomeCheckbox);

                for (CheckBox checkBox : nomeCheckbox) checkBox.setOnAction(e -> updateLabel());
                c.setOnAction(e -> equally(nomeCheckbox));
            }
        });

        cbPaidBy.setVisibleRowCount(4);
    }
    public void openClose() {
        scrollpaneCheckbox.setVisible(!scrollpaneCheckbox.isVisible());
    }
    public void onDone(){
        scrollpaneCheckbox.setVisible(false);
        lberror.setStyle("-fx-text-fill: #fd7777;");
        lberror.setVisible(true);

        if(tfDescription.getText().isEmpty()) lberror.setText("Description cannot be empty");
        else if(datePicker.getValue() == null) lberror.setText("Date cannot be empty");
        else if (tfAmount.getText().isEmpty()) lberror.setText("Amount cannot be empty");
        else if(cbGroup.getValue().isEmpty()||cbGroup.getValue().equals("Choose a group")) lberror.setText("Group cannot be empty");
        else if(cbPaidBy.getValue().isEmpty() || cbPaidBy.getValue().isBlank()) lberror.setText("Paid by cannot be empty");
        else if(lbSelected.getText().equals("Select options")) lberror.setText("Split cannot be empty");
        else {
            LocalDate selectedDate = datePicker.getValue();
            String formattedDate = selectedDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

            String resposta = client.mensagemAddExpense(cbGroup.getValue(), tfDescription.getText(), Float.parseFloat(tfAmount.getText()), formattedDate, cbPaidBy.getValue(), lbSelected.getText());
            lberror.setText(resposta);

            if (resposta.equals("Expense added successfully!")) {
                lberror.setStyle("-fx-text-fill: #1DB794;");
                clean();
            }
        }
    }

    private void clean (){
        tfAmount.setText("");
        tfAmount.setPromptText("€");
        datePicker.setValue(null);
        tfDescription.setText("");
        tfDescription.setPromptText("Description");
        cbGroup.setValue("Choose a group");
        cbPaidBy.setValue("You");
        lbSelected.setText("Select options");
        lberror.setVisible(false);
    }
    private void equally(ObservableList<CheckBox> nomeCheckbox){
        scrollpaneCheckbox.setVisible(false);
        for (CheckBox checkBox : nomeCheckbox) {
            checkBox.setDisable(true);
            checkBox.setSelected(false);
            lbSelected.setText("Equally");
        }
        if (!nomeCheckbox.getFirst().isSelected())
            for (CheckBox checkBox : nomeCheckbox)
                checkBox.setDisable(false);
    }
    private void updateLabel() {
        scrollpaneCheckbox.setVisible(false);
        ObservableList<String> selectedNames = FXCollections.observableArrayList();

        for (Node node : vbCheckboxs.getChildren())
            if(node instanceof CheckBox checkBox)
                if (checkBox.isSelected())
                    selectedNames.add(checkBox.getText());

        lbSelected.setText(selectedNames.isEmpty() ? "Select options" : String.join(", ", selectedNames));
    }
    private void amountFormat() {
        Pattern pattern = Pattern.compile("^\\d*\\.?\\d{0,2}$");
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (pattern.matcher(newText).matches()) {
                return change;
            }
            return null;
        };
        StringConverter<String> converter = new StringConverter<>() {
            @Override
            public String fromString(String string) {
                return string;
            }
            @Override
            public String toString(String object) {
                return object;
            }
        };
        TextFormatter<String> textFormatter = new TextFormatter<>(converter, "", filter);
        tfAmount.setTextFormatter(textFormatter);
    }
}
