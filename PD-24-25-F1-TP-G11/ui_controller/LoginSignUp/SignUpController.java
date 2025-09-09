package pt.isec.pd.trabalho_pratico.ui_controller.LoginSignUp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.Pane;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import javafx.application.Platform;
import pt.isec.pd.trabalho_pratico.Client.Client;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class SignUpController {
    @FXML
    public Button btnCancel;
    @FXML
    public Button btnDone;
    @FXML
    private Pane contentPane;
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

    @FXML
    public void initialize() {
        client = Client.getInstance();
        tfPhoneFormat();
    }

    private void tfPhoneFormat() { // só aceita numeros
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
        tfPhone.setTextFormatter(textFormatter);
    }

    public void onDone(){
        //com sucesso
        if(tfName.getText().isEmpty()||tfName.getText().isBlank()){
            lberror.setText("Name cannot be empty");
            lberror.setVisible(true);
        }else if(tfPhone.getText().isEmpty()){
            lberror.setText("Phone Number cannot be empty");
            lberror.setVisible(true);
        }else if(tfEmail.getText().isEmpty()){
            lberror.setText("Email cannot be empty");
            lberror.setVisible(true);
        }else if(tfPassword.getText().isEmpty()){
            lberror.setText("Password cannot be empty");
            lberror.setVisible(true);
        }else{
            String resposta = client.mensagemSigUp(tfEmail.getText(), tfPhone.getText(), tfPassword.getText(), tfName.getText());
            if (resposta.equals("Conexão terminada com o servidor.")) Platform.exit();
            lberror.setText(resposta);

            lberror.setVisible(true);

            if(client.isLogin())
                loadFXML("/resources/fxml/Bar.fxml");
        }
    }
    public void onCancel(){
        loadFXML("/resources/fxml/FirstPage.fxml");
    }

    private void loadFXML(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            Node node = loader.load();
            contentPane.getChildren().setAll(node);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}