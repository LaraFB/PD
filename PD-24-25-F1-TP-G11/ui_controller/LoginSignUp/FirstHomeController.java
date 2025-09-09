package pt.isec.pd.trabalho_pratico.ui_controller.LoginSignUp;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import pt.isec.pd.trabalho_pratico.Client.Client;

public class FirstHomeController {
    @FXML
    public Button btnLogin;
    @FXML
    public Button btnRegister;
    @FXML
    private Pane contentPane;

    @FXML
    public void initialize() {
        Client client = Client.getInstance();
        if (!client.connect()) {
            System.out.println("Falha ao conectar com o servidor.");
            Platform.exit();
        }
    }

    public void onLogin(){
        loadFXML("/resources/fxml/LoginPage.fxml");
    }
    public void onRegister(){
        loadFXML("/resources/fxml/RegisterPage.fxml");
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
