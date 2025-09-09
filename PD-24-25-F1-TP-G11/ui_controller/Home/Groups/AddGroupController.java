package pt.isec.pd.trabalho_pratico.ui_controller.Home.Groups;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import pt.isec.pd.trabalho_pratico.Client.Client;

public class AddGroupController {
    @FXML
    private TextField tfGroup;
    @FXML
    private Label lberror;
    @FXML
    private Pane pane;
    private Client client;


    public void initialize() {
        client = Client.getInstance();
        lberror.setVisible(false);
    }

    public void onDone() {
        lberror.setStyle("-fx-text-fill: #FD7777;");
        if(tfGroup.getText().isEmpty()) {
            lberror.setVisible(true);
            lberror.setText("Group Name cannot be empty");
        }else{
            lberror.setText(client.mensagemAddGrupo(tfGroup.getText()));
            if(lberror.getText().contains("Grupo criado com sucesso!"))
                loadFXML();
            lberror.setVisible(true);
        }
    }
    public void onCancel() {
        loadFXML();
    }

    private void loadFXML() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/GroupPage.fxml"));
            Node node = loader.load();
            pane.getChildren().setAll(node);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
