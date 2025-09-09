package pt.isec.pd.trabalho_pratico.ui_controller.Home.Groups.GroupDetails.Edit;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import pt.isec.pd.trabalho_pratico.Client.Client;
import pt.isec.pd.trabalho_pratico.ui_controller.Home.Groups.GroupDetails.GroupDetailsController;

import javax.swing.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class EditExpenseController {
    @FXML
    private Button btnDelete;
    @FXML
    private Button btnEliminarPagamento;
    @FXML
    private Button btnPagar;
    @FXML
    private AnchorPane pane;
    @FXML
    private TextField tfAmount;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TextField tfDescription;
    @FXML
    private ComboBox<String> cbPaidBy;
    @FXML
    private Label lberror;
    @FXML
    private ScrollPane scrollpaneCheckbox;
    @FXML
    private Label lbSelected;
    @FXML
    private VBox vbCheckboxs;
    private String nome_grupo;
    private String atualExpense;
    private Client client;

    public void initialize() {
        client = Client.getInstance();
        amountFormat();

        lberror.setVisible(false);
        scrollpaneCheckbox.setVisible(false);
        lbSelected.setText("Select options");

        String [] nomes = client.getTodosNomesPessoas().split(",");
        ObservableList<CheckBox> nomeCheckbox =  FXCollections.observableArrayList();

        CheckBox c = new CheckBox("Equally");
        nomeCheckbox.add(c);

        for (String a : nomes){
            CheckBox aux = new CheckBox(a);
            nomeCheckbox.add(aux);
            cbPaidBy.getItems().add(a);
        }
        vbCheckboxs.getChildren().addAll(nomeCheckbox);

        for (CheckBox checkBox : nomeCheckbox) checkBox.setOnAction(event -> updateLabel());
        c.setOnAction(event -> equally(nomeCheckbox));
        cbPaidBy.setVisibleRowCount(4);
    }
    public void setExpense(String nome_grupo, String descricao, String date,  String paidBy, String split, String preco){
        this.nome_grupo = nome_grupo;
        String [] info = client.accountInfo();
        String [] pessoas = split.split(" ");
        ObservableList<String> selectedNames = FXCollections.observableArrayList();
        setFieldsEditable(false);

        tfAmount.setText(preco);
        tfDescription.setText(descricao);
        datePicker.setValue(LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy/MM/dd")));
        if(paidBy.equalsIgnoreCase("you")) cbPaidBy.setValue(info[0]);
        else cbPaidBy.setValue(paidBy);

        for (Node node : vbCheckboxs.getChildren()) {
            if (node instanceof CheckBox checkBox) {
                for (String pessoa : pessoas) {
                    if (checkBox.getText().equals(pessoa)) {
                        checkBox.setSelected(true);
                        selectedNames.add(checkBox.getText());
                    }
                }
            }
        }
        lbSelected.setText(selectedNames.isEmpty() ? "Select options" : String.join(", ", selectedNames));
        atualExpense = nome_grupo + "-" + descricao + "-"  + preco + "-" + date + "-" + paidBy + "-" + split;

        boolean b  = client.youPaid(atualExpense);
        btnPagar.setDisable(b);
        btnEliminarPagamento.setDisable(!b);

        lberror.setVisible(false);
    }
    public void onEdit() {
        lberror.setVisible(false);
        setFieldsEditable(true);
    }
    public void onDelete() {
        lberror.setVisible(false);
        String resposta = client.mensagemDeleteExpense(atualExpense);

        if(resposta.equals("Despesa Eliminada")) loadFXML(nome_grupo);
        else JOptionPane.showMessageDialog(null, resposta,"Error", JOptionPane.ERROR_MESSAGE);
    }
    public void onPagar() {
        lberror.setVisible(false);
        String resposta =  client.mensagemPagarDespesa(atualExpense);
        if(resposta.equals("Expense paid successfully")) JOptionPane.showMessageDialog(null, resposta, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        else JOptionPane.showMessageDialog(null, resposta,"Error", JOptionPane.ERROR_MESSAGE);
        boolean b  = client.expenseIsPaid(atualExpense);
        btnPagar.setDisable(b);
        btnEliminarPagamento.setDisable(!b);
    }
    public void onEliminarPagamento() {
        lberror.setVisible(false);
        String resposta = client.cancelarPagamento(atualExpense);
        if(resposta.equals("Payment canceled successfully")) JOptionPane.showMessageDialog(null, resposta, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        else JOptionPane.showMessageDialog(null, resposta,"Error", JOptionPane.ERROR_MESSAGE);
        boolean b  = client.expenseIsPaid(atualExpense);
        btnPagar.setDisable(b);
        btnEliminarPagamento.setDisable(!b);
    }
    public void openClose() {
        if (!lbSelected.isDisable())
            scrollpaneCheckbox.setVisible(!scrollpaneCheckbox.isVisible());
    }
    public void onDone(){
        scrollpaneCheckbox.setVisible(false);
        lberror.setStyle("-fx-text-fill: #fd7777;");

        if(tfDescription.getText().isEmpty()) lberror.setText("Description cannot be empty");
        else if(datePicker.getValue() == null) lberror.setText("Date cannot be empty");
        else if (tfAmount.getText().isEmpty()) lberror.setText("Amount cannot be empty");
        else if(cbPaidBy.getValue().isEmpty()) lberror.setText("Paid by cannot be empty");
        else if(lbSelected.getText().equals("Select options")) lberror.setText("Split cannot be empty");
        else{
            String formattedDate = datePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String resposta = client.mensagemEditExpense(atualExpense,tfDescription.getText(),tfAmount.getText(),formattedDate,cbPaidBy.getValue(),lbSelected.getText());
            lberror.setText(resposta);
        }
        if (lberror.getText().equals("Expense edited successfully!")){
            lberror.setStyle("-fx-text-fill: #1DB794;");
            setExpense(nome_grupo,tfDescription.getText(),datePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")), cbPaidBy.getValue(), lbSelected.getText(), tfAmount.getText());
            loadFXML(nome_grupo);
        }
        else {
            String[] args = atualExpense.split("-");
            setExpense(args[0], args[1], args[3], args[4], args[5], args[2]);
        }
        lberror.setVisible(true);
        setFieldsEditable(false);
    }

    private void loadFXML(String groupName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/GroupDetailsPage.fxml"));
            Node node = loader.load();

            GroupDetailsController groupDetailsController = loader.getController();
            groupDetailsController.initialize(groupName);

            pane.getChildren().setAll(node);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
    private void amountFormat() { // só aceita numeros
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
    private void setFieldsEditable(boolean editable) {
        if (editable) {
            tfAmount.setEditable(true);
            datePicker.setDisable(false);
            tfDescription.setEditable(true);
            cbPaidBy.setDisable(false);
            lbSelected.setDisable(false);

            btnDelete.setDisable(true);
            btnPagar.setDisable(true);
            btnEliminarPagamento.setDisable(true);

            updateStyleClass(tfAmount, "tfEditable", "textField");
            datePicker.setStyle("-fx-text-fill: #545454;");
            updateStyleClass(tfDescription, "tfEditable", "textField");
            cbPaidBy.setStyle("-fx-text-fill: #545454;");
            lbSelected.setStyle("-fx-text-fill: #545454;-fx-background-color: #f0f3f5;");
        } else {
            btnDelete.setDisable(false);
            tfAmount.setEditable(false);
            datePicker.setDisable(true);
            tfDescription.setEditable(false);
            cbPaidBy.setDisable(true);
            lbSelected.setDisable(true);

            datePicker.setStyle("-fx-opacity: 50%; -fx-background-color: #f0f3f5;");
            updateStyleClass(tfAmount, "textField", "tfEditable");
            updateStyleClass(tfDescription, "textField", "tfEditable");
            cbPaidBy.setStyle("-fx-opacity: 50%;-fx-background-color: #f0f3f5;");
            lbSelected.setStyle("-fx-opacity: 50%;-fx-background-color: #f0f3f5;");
        }
    }
    private void updateStyleClass(TextField textField, String removeClass, String addClass) {
        textField.getStyleClass().remove(removeClass);
        if (!textField.getStyleClass().contains(addClass)) {
            textField.getStyleClass().add(addClass);
        }
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

    public void onBack() {
        loadFXML(nome_grupo);
    }
}
