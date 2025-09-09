package pt.isec.pd.trabalho_pratico.ui_controller.LoginSignUp;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import pt.isec.pd.trabalho_pratico.Client.Client;

public class LoginController {
    @FXML
    public Button btnCancel;
    @FXML
    public Button btnDone;
    @FXML
    private Pane contentPane;
    @FXML
    private Label lberror;
    @FXML
    private PasswordField password;
    @FXML
    private TextField username;

    private Client client;

    @FXML
    public void initialize() {
        client = Client.getInstance();
    }

    public void onDone(){
        if(username.getText().isEmpty()||username.getText().isBlank()){
            lberror.setText("Username cannot be empty");
            lberror.setVisible(true);
        }
        else if(password.getText().isEmpty()){
            lberror.setText("Password cannot be empty");
            lberror.setVisible(true);
        }
        else{
            String resposta = client.mensagemLogin(username.getText(), password.getText());
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

