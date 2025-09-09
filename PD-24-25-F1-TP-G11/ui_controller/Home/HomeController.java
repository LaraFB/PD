package pt.isec.pd.trabalho_pratico.ui_controller.Home;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import pt.isec.pd.trabalho_pratico.Client.Client;

public class HomeController {
    @FXML
    private Text txOwe;
    @FXML
    private Text txOwed;
    @FXML
    private Text txTotal;
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

    private void update(){
        cbowe.getItems().clear();
        cbGroup.getItems().clear();
        cbGroup.getItems().add("All");

        String [] grupos = client.getNomesGrupoPertence().split(",");
        for (String grupo : grupos)  cbGroup.getItems().add(grupo);
        cbowe.getItems().addAll("Owe","Owed");
        cbowe.setValue("Owe");

        cbGroup.setValue("All");
        cbGroup.valueProperty().addListener((observable, oldValue, newValue) -> mostraDespesas());


        cbowe.valueProperty().addListener((observable, oldValue, newValue) -> mostraDespesas());
        mostraDespesas();
    }

    public void addNewLabel (String nome, float payvalue){
        String  texto = formartaString(nome,payvalue);

        Label label = new Label(texto);
        label.getStyleClass().add("labelHomePage");
        vbLabels.getChildren().add(label);
    }

    private void mostraDespesas() {
        clean();
        String grupo = cbGroup.getValue();
        String[] despesas = client.getDespesasHome(grupo, cbowe.getValue()).split(",");
        float dividas = Float.parseFloat(client.getGrupoDividas(grupo));
        float porReceber = Float.parseFloat(client.getGrupoPorReceber(grupo));
        float balance = porReceber - dividas;

        if(despesas.length > 1)
            for (int i = 0; i < despesas.length; i += 2)
                addNewLabel(despesas[i], Float.parseFloat(despesas[i + 1]));

        txOwe.setText(String.format("%.2f€", dividas));
        txOwed.setText(String.format("%.2f€", porReceber));
        txTotal.setText(String.format("%.2f€", balance));
    }
    private void clean() {
        vbLabels.getChildren().clear();
    }
    private String formartaString (String descricao, float valor){
        String valorS;
        String[]descricaoCompleta = descricao.split(" ");

        if(descricaoCompleta.length > 1)
            descricao = "%s %s".formatted(descricaoCompleta[0], descricaoCompleta[descricaoCompleta.length - 1]);

        if (valor % 1 == 0)
            valorS = String.format("%d", (int) valor);
        else {
            valorS = String.format("%.2f", Math.abs(valor));
            if (valorS.endsWith("0"))
                valorS = valorS.substring(0, valorS.length() - 1);
        }

        return "%s %s€".formatted(descricao, valorS);
    }
}