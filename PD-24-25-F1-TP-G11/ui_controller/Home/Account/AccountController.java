package pt.isec.pd.trabalho_pratico.ui_controller.Home.Account;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import pt.isec.pd.trabalho_pratico.Client.Client;

public class AccountController {
    @FXML
    private Text tName;
    @FXML
    private TextField tfName;
    @FXML
    private TextField tfEmail;
    @FXML
    private TextField tfPassword;
    @FXML
    private TextField tfPhone;
    @FXML
    private Label lberror;
    private Client client;

    public String getTfEmail() {
        return tfEmail.getText();
    }
    public void setTfEmail(String email) {
        tfEmail.setText(email);
    }
    public String getTfName() {
        return tfName.getText();
    }
    public void setTfName(String name) {
        tfName.setText(name);
    }
    public String getTfPassword() {
        return tfPassword.getText();
    }
    public void setTfPassword(String password) {
        tfPassword.setText(password);
    }
    public String getTfPhone() {
        return tfPhone.getText();
    }
    public void setTfPhone(String phone) {
        tfPhone.setText(phone);
    }

    public void initialize(){
        client = Client.getInstance();
        String [] info = client.accountInfo();

        setTfEmail(info[0]);
        setTfPhone(info[1]);
        setTfPassword(info[2]);
        setTfName(info[3]);
        tName.setText("Hi "+ info[3] +"!");

        setFieldsEditable(false);
        lberror.setVisible(false);
    }
    public void onCancel(){
        atualizaInfo();
        setFieldsEditable(false);
    }
    public void onEdit(){
        tfName.getStyleClass().add("textField");
        tfEmail.getStyleClass().add("textField");
        tfPassword.getStyleClass().add("textField");
        tfPhone.getStyleClass().add("textField");
        setFieldsEditable(true);
    }
    public void onDone(){
        String msg = client.mensagemEditAccount(getTfEmail(), getTfPhone(), getTfPassword(), getTfName());
        lberror.setText(msg);
        lberror.setVisible(true);

        if(!msg.equals("Account info changed"))
            lberror.setStyle("-fx-text-fill: #FD7777");
        else
            lberror.setStyle("-fx-text-fill: #1DB794");
        setFieldsEditable(false);
    }

    private void atualizaInfo (){
        String [] info = client.accountInfo();
        tfName.setText(info[3]);
        tfEmail.setText(info[0]);
        tfPassword.setText(info[2]);
        tfPhone.setText(info[1]);
    }
    private void setFieldsEditable(boolean editable) {
        if (editable) {
            tfName.setEditable(true);
            tfEmail.setEditable(true);
            tfPassword.setEditable(true);
            tfPhone.setEditable(true);

            updateStyleClass(tfName, "tfEditable", "textField");
            updateStyleClass(tfEmail, "tfEditable", "textField");
            updateStyleClass(tfPassword, "tfEditable", "textField");
            updateStyleClass(tfPhone, "tfEditable", "textField");
        } else {
            tfName.setEditable(false);
            tfEmail.setEditable(false);
            tfPassword.setEditable(false);
            tfPhone.setEditable(false);

            updateStyleClass(tfName, "textField", "tfEditable");
            updateStyleClass(tfEmail, "textField", "tfEditable");
            updateStyleClass(tfPassword, "textField", "tfEditable");
            updateStyleClass(tfPhone, "textField", "tfEditable");
        }
    }
    private void updateStyleClass(TextField textField, String removeClass, String addClass) {
        textField.getStyleClass().remove(removeClass);
        if (!textField.getStyleClass().contains(addClass)) {
            textField.getStyleClass().add(addClass);
        }
    }

    public void onLogOut() {
        client.logOut();
        Platform.exit();
    }
}
