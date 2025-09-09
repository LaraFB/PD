package pt.isec.pd.trabalho_pratico.ui_controller.Home.History;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import pt.isec.pd.trabalho_pratico.Client.Client;

public class HistoryController {
    @FXML
    private VBox vbLabels;
    @FXML
    private ComboBox<String> cbGroup;
    @FXML
    private ComboBox<String> cbowe;
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
        cbowe.getItems().clear();
        cbGroup.getItems().clear();
        cbGroup.getItems().add("All");

        String [] grupos = client.getNomesGrupoPertence().split(",");
        for (String grupo : grupos)  cbGroup.getItems().add(grupo);
        cbowe.getItems().addAll("Paid you","You paid");
        cbowe.setValue("You paid");

        cbGroup.setValue("All");
        cbGroup.valueProperty().addListener((observable, oldValue, newValue) -> mostraDespesas());
        cbowe.valueProperty().addListener((observable, oldValue, newValue) -> mostraDespesas());
        mostraDespesas();
    }
    public void addNewLabel (String descricao, String grupo, float paidvalue){
        String valorS = String.format("%.2f", Math.abs(paidvalue));
        String texto = descricao + " " + valorS + "€" + " from " + grupo ;

        Label label = new Label(texto);
        label.getStyleClass().add("labelActivityPage");
        vbLabels.getChildren().add(label);
    }

    private void mostraDespesas() {
        clean();
        String[] despesas = client.getDespesasHistorico(cbGroup.getValue(), cbowe.getValue()).split(",");

        if(despesas.length > 1)
            for (int i = 0; i < despesas.length; i += 3)
                addNewLabel(despesas[i],despesas[i+2], Float.parseFloat(despesas[i + 1]));

    }
    private void clean() {
        vbLabels.getChildren().clear();
    }
}
