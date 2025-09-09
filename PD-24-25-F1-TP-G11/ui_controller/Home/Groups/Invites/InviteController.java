package pt.isec.pd.trabalho_pratico.ui_controller.Home.Groups.Invites;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import pt.isec.pd.trabalho_pratico.Client.Client;

import java.io.IOException;

public class InviteController {
    @FXML
    private Label lbNoInvites;
    @FXML
    private VBox vbInvites;
    @FXML
    private Pane pane;
    private Client client;
    private int ninvites;

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
    public void addInvite(String nomeGrupo) {
        HBox hbInvite = new HBox();
        Label lbInviteGroup = new Label(nomeGrupo);
        lbInviteGroup.getStyleClass().add("labelInvite");

        Button btnAccept = new Button();
        Button btnDecline = new Button();
        btnAccept.getStyleClass().add("btnInvites");
        btnDecline.getStyleClass().add("btnInvites");

        Image imageA = new Image((String.valueOf(getClass().getResource("/resources/images/accept_icon.png"))));
        ImageView imageViewA = new ImageView(imageA);
        btnAccept.setGraphic(imageViewA);
        imageViewA.setFitHeight(15);
        imageViewA.setFitWidth(15);

        Image imageD = new Image((String.valueOf(getClass().getResource("/resources/images/cancel_icon.png"))));
        ImageView imageViewD = new ImageView(imageD);
        btnDecline.setGraphic(imageViewD);
        imageViewD.setFitHeight(15);
        imageViewD.setFitWidth(15);

        hbInvite.getChildren().add(lbInviteGroup);
        hbInvite.getChildren().add(btnAccept);
        hbInvite.getChildren().add(btnDecline);
        vbInvites.getChildren().add(hbInvite);
        ninvites ++;

       btnAccept.setOnAction(event -> {  //Adicionar novo grupo na lista dos grupos
        if(client.mensagemAceitarInvite(nomeGrupo)){
            vbInvites.getChildren().remove(hbInvite);
            ninvites --;
            lbNoInvites.setVisible(ninvites == 0);
        }
       });

       btnDecline.setOnAction(event -> { //eliminar convite
            if(client.mensagemRejeitarConvite(nomeGrupo)){
                vbInvites.getChildren().remove(hbInvite);
                ninvites --;
                lbNoInvites.setVisible(ninvites == 0);
            }
       });
    }
    public void onAddInvite() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/AddInvitePage.fxml"));
        Node node = loader.load();
        pane.getChildren().setAll(node);
    }

    private void update() {
        ninvites = 0;
        vbInvites.getChildren().clear();
        vbInvites.getChildren().add(lbNoInvites);
        String resposta = client.getInvites();

        if(!resposta.isEmpty()){
            String [] nomes = resposta.split(",");
            for (String nome : nomes) addInvite(nome);
        }
        lbNoInvites.setVisible(ninvites == 0);
    }
}
