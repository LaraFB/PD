package pt.isec.pd.trabalho_pratico.ui_controller.Home.Groups;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import pt.isec.pd.trabalho_pratico.Client.Client;
import pt.isec.pd.trabalho_pratico.ui_controller.Home.Groups.GroupDetails.GroupDetailsController;

public class GroupController {
    @FXML
    private VBox vbGroups;
    @FXML
    private Pane pane;
    private Client client;

    public void initialize() {
        client = Client.getInstance();
        update();
        client.needsUpdate().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                Platform.runLater(() -> {
                    update();
                    client.resetUpdate();
                });
            }
        });
    }
    private void update() {
        vbGroups.getChildren().clear();
        String resposta = client.getNomesGrupoPertence();

        if(!resposta.isEmpty()){
            String [] nomes = resposta.split(",");
            for (String nome : nomes) addNewGroup(nome);
        }
    }
    public void onAddGroup(){
        loadFXML("/resources/fxml/AddGroupPage.fxml","");
    }
    public void onInvite(){
        loadFXML("/resources/fxml/InvitePage.fxml","");
    }
    public void onClickGroup(String groupName){
        loadFXML("/resources/fxml/GroupDetailsPage.fxml", groupName);
    }

    private void loadFXML(String fxmlFileName, String groupName) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            Node node = loader.load();

            if (fxmlFileName.equals("/resources/fxml/GroupDetailsPage.fxml")) {
                GroupDetailsController groupDetailsController = loader.getController();
                groupDetailsController.initialize(groupName);
            }
            pane.getChildren().setAll(node);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private void addNewGroup(String groupName) {
        Label lbGroup = new Label(groupName);
        lbGroup.getStyleClass().add("groupLabel");
        vbGroups.getChildren().add(lbGroup);
        lbGroup.setOnMouseClicked(event -> onClickGroup(groupName));
    }
}
