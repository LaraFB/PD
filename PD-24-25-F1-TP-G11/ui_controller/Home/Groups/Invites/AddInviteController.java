package pt.isec.pd.trabalho_pratico.ui_controller.Home.Groups.Invites;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import pt.isec.pd.trabalho_pratico.Client.Client;

public class AddInviteController {
    @FXML
    private Label lberror;
    @FXML
    private ComboBox<String> cbGroup;
    @FXML
    private ComboBox<String> cbPessoa;
    @FXML
    private Pane pane;
    private Client client;

    public void initialize() {
        client = Client.getInstance();
        String [] nomes = client.getNomesPessoas().split(",");
        String [] grupos = client.getNomesGrupoPertence().split(",");

        for (String nome : nomes)  cbPessoa.getItems().add(nome);
        for (String grupo : grupos)  cbGroup.getItems().add(grupo);

        cbGroup.setValue("Group Name");
        cbPessoa.setValue("Person Name");
        cbGroup.setVisibleRowCount(5);
        cbPessoa.setVisibleRowCount(5);
        lberror.setVisible(false);
    }

    public void onDone() {
        if(cbGroup.getValue().equals("Group Name")){
            lberror.setVisible(true);
            lberror.setText("Group Name cannot be empty");
        }
        else if(cbPessoa.getValue().equals("Person Name")){
            lberror.setVisible(true);
            lberror.setText("Person Name cannot be empty");
        }
        else{
            lberror.setText(client.mensagemInvite(cbGroup.getValue(),cbPessoa.getValue()));
            lberror.setVisible(true);
            if(lberror.getText().equals("Convite Enviado!"))
                load();
        }
    }
    public void onCancel() {
        load();
    }

    private void load(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/InvitePage.fxml"));
            Node node = loader.load();
            pane.getChildren().setAll(node);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
