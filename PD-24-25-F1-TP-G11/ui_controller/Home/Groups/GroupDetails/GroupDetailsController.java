package pt.isec.pd.trabalho_pratico.ui_controller.Home.Groups.GroupDetails;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import pt.isec.pd.trabalho_pratico.Client.Client;
import pt.isec.pd.trabalho_pratico.ui_controller.Home.Groups.GroupDetails.Edit.EditExpenseController;

import javax.swing.*;
import java.io.IOException;

public class GroupDetailsController {
    @FXML
    private VBox vbexpense;
    @FXML
    private Label lbGroupName;
    @FXML
    private TextField tfGroupName;
    @FXML
    private VBox vbGroup;
    @FXML
    private Pane pane;
    @FXML
    private Label lbOwe;
    @FXML
    private Label lbOwed;
    @FXML
    private Label lbGroupTotal;
    private Client client;
    private int id;

    public void initialize(String groupName) {
        client = Client.getInstance();
        id = client.getIDGrupo(groupName);
        update(id);

        client.needsUpdate().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                Platform.runLater(() -> {
                    update(id);
                    client.resetUpdate();
                });
            }
        });
    }
    private void update (int id) {
        vbexpense.getChildren().clear();
        String groupName = client.getNomeGrupoById(id);
        if(groupName.equals("There's no such grupo!"))
            load("/resources/fxml/GroupPage.fxml");
        else{
            lbGroupName.setText(groupName);
            lbGroupName.setStyle("-fx-alignment: center; -fx-font-size: 36px; -fx-font-family: 'Berlin Sans FB'");

            lbOwe.setText(String.format("%.2f€", Float.parseFloat(client.getGrupoDividas(lbGroupName.getText()))));
            lbOwed.setText(String.format("%.2f€", Float.parseFloat(client.getGrupoPorReceber(lbGroupName.getText()))));
            lbGroupTotal.setText(String.format("%.2f€", Float.parseFloat(client.getGrupoTotal(lbGroupName.getText()))));

            //Adicionar despesas
             String despesas = client.getDespesasPorGrupo(lbGroupName.getText());
             String [] despesa = despesas.split("\\|");
             String dataant = "0000/00";

             if (!despesas.equals("Nao existe despesas!")) {
                 for (int i = 0, iLabel, iExpense=0; i < despesa.length; i ++) { // 0 -> descricao 1-> valor 2-> data 3-> quem pagou 4 ->slpit with 5-> valor individual

                     String [] args = despesa[i].split(";");
                     String data = args[2].substring(0,7);

                     iLabel = dataant.equals("0000/00") ? 0 : dataant.equals(data) ? -1 : iExpense +1;
                     iExpense = (iLabel == -1) ? iExpense + 1 : iLabel + 1;
                     if(client.isYou(args[3])) args[3] = "You";

                     addNewExpense(args[0], args[1], args[2], args[3],  args[5], args[4], iExpense, iLabel);
                     dataant = data;
                 }
            }
        }
    }

    public void onLeaveGroup() {
        String resposta = client.mensagemLeaveGroup(lbGroupName.getText());

        if(resposta.equals("Saiu do grupo!")) load("/resources/fxml/GroupPage.fxml");
        else JOptionPane.showMessageDialog(null, resposta,"Error", JOptionPane.ERROR_MESSAGE);
    }
    public void onDeleteGroup() {
        String resposta = client.mensagemDeleteGroup(lbGroupName.getText());
        if(resposta.equals("Grupo apagado")) load("/resources/fxml/GroupPage.fxml");
        else JOptionPane.showMessageDialog(null, resposta, "Error", JOptionPane.ERROR_MESSAGE);
    }
    public void onAddPeopleToGroup() {
        load("/resources/fxml/AddInvitePage.fxml");
    }
    public void onDownloadExpense() {
        String resposta = client.downloadDespesas(lbGroupName.getText());
        if(resposta.equals("File created successfully!"))  JOptionPane.showMessageDialog(null, resposta, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        else JOptionPane.showMessageDialog(null, resposta, "Error", JOptionPane.ERROR_MESSAGE);
    }
    public void onEditGroup() {
        String old_name = lbGroupName.getText();
        tfGroupName = new TextField();
        tfGroupName.setStyle("-fx-background-color: white; -fx-text-fill:  #545454; -fx-font-family: 'Berlin Sans FB'; -fx-font-size: 36px; -fx-pref-width: 335px; -fx-pref-height: 70px; -fx-alignment: center;");
        tfGroupName.setText(old_name);
        vbGroup.getChildren().removeFirst();
        vbGroup.getChildren().addFirst(tfGroupName);

        tfGroupName.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String new_name = tfGroupName.getText();

                if(!new_name.isEmpty()) {
                    lbGroupName.setText(client.mensagemChangeName(old_name, new_name) ? new_name : old_name);
                    if (lbGroupName.getText().equals(old_name))
                        JOptionPane.showMessageDialog(null, "Invalid Name", "Error", JOptionPane.ERROR_MESSAGE);
                    vbGroup.getChildren().removeFirst();
                    vbGroup.getChildren().addFirst(lbGroupName);
                    lbGroupName.setStyle("-fx-alignment: center; -fx-font-size: 36px; -fx-font-family: 'Berlin Sans FB'");
                }else
                    JOptionPane.showMessageDialog(null, "Invalid Name", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void load( String filename){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(filename));
            Node node = loader.load();
            pane.getChildren().setAll(node);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private void addNewExpense(String descricaoExpense, String precototal, String dateExpense, String paidBy, String preco, String split, int iExpense, int iLabel) {
        String [] data =dateExpense.split("/");
        String atualExpense = "%s-%s-%s-%s-%s-%s".formatted(lbGroupName.getText(), descricaoExpense, precototal, dateExpense, paidBy, split);

        if(iLabel != -1){
            Label lbmesano = new Label(data[1] + " " + data[0]);
            lbmesano.setStyle("-fx-background-color: #f0f3f5;-fx-pref-width: 335px;-fx-font-size: 12px;-fx-pref-height: 20px; -fx-text-fill: #545454;");
            vbexpense.getChildren().add(iLabel, lbmesano);
        }

        HBox hbExpense = new HBox();
        Label lbDia = new Label(String.valueOf(data[2]));
        lbDia.getStyleClass().add("labelGroup");
        lbDia.setStyle("-fx-font-weight: bold; -fx-pref-height: 40px; -fx-pref-width: 40px; -fx-alignment: CENTER;");

        VBox vbdetalhes = new VBox();
        Label lbname = new Label(descricaoExpense);
        lbname.getStyleClass().add("labelGroup");
        lbname.setStyle("-fx-font-weight: bold;-fx-pref-height: 20px; -fx-pref-width: 190px;");
        Label lbdescricao = new Label(paidBy + String.format(" payed %.2f€", Float.parseFloat(precototal)));
        lbdescricao.getStyleClass().add("labelGroup");
        lbdescricao.setStyle("-fx-pref-height: 5px; -fx-pref-width: 190px;-fx-font-size: 10px; -fx-alignment: CENTER;");
        vbdetalhes.getChildren().addAll(lbname, lbdescricao);

        VBox vbpreco = new VBox();
        Label lbyou;
        Label lbpreco = new Label(String.format("%.2f€", Float.parseFloat(preco)));
        boolean pago = client.expenseIsPaid(atualExpense);
        boolean tuPagaste = client.youPaid(atualExpense);
        String[] info =client.accountInfo();

        if(paidBy.equals("You") && !pago && tuPagaste){
            lbyou = new Label("You're owed");
            lbyou.getStyleClass().add("labelGroup");
            lbpreco = new Label(String.format("%.2f€", Float.parseFloat(preco)*(split.split(" ").length-1)));
            lbyou.setStyle("-fx-text-fill:#1DB794;-fx-pref-height: 20px; -fx-pref-width: 90px;");
            lbpreco.setStyle("-fx-alignment:center;-fx-text-fill:#1DB794; -fx-font-weight: bold;-fx-pref-height: 20px; -fx-pref-width: 90px; -fx-font-size: 18px;");
        } else if(!tuPagaste && split.contains(info[0])){
            lbyou = new Label("You owe");
            lbyou.getStyleClass().add("labelGroup");
            lbyou.setStyle("-fx-text-fill:#FD7777;-fx-pref-height: 20px; -fx-pref-width: 90px");
            lbpreco.setStyle("-fx-alignment:center;-fx-text-fill:#FD7777; -fx-font-weight: bold;-fx-pref-height: 20px; -fx-pref-width: 90px;-fx-font-size: 18px;");

        }else {
            lbyou = new Label("");
            lbyou.setStyle("-fx-text-fill:#545454;-fx-pref-height: 0px; -fx-pref-width: 90px;");
            lbpreco.setStyle("-fx-alignment:center;-fx-text-fill:#545454; -fx-font-weight: bold;-fx-pref-height: 20px; -fx-pref-width: 90px; -fx-font-size: 18px;");
        }

        vbpreco.getChildren().addAll(lbyou, lbpreco);
        hbExpense.getChildren().addAll(lbDia, vbdetalhes, vbpreco);

        hbExpense.setOnMouseEntered(event -> hbExpense.setStyle("-fx-background-color: #f0f3f5;-fx-cursor: hand;"));
        hbExpense.setOnMouseExited(event -> hbExpense.setStyle("-fx-border-color: #f0f3f5; -fx-border-width: 0 0 1 0;-fx-pref-width: 315px;-fx-pref-height: 50px;-fx-padding: 0 0 5px 0;"));
        vbexpense.getChildren().add(iExpense, hbExpense);

        hbExpense.setOnMouseClicked(event -> onClickExpense(descricaoExpense, dateExpense, paidBy, split,precototal));
    }
    private void onClickExpense(String descricao, String dateExpense, String paidBy, String split, String preco) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/EditExpensePage.fxml"));
            Node node = loader.load();

            EditExpenseController editExpenseController = loader.getController();
            editExpenseController.setExpense(lbGroupName.getText(),descricao,dateExpense,paidBy,split,preco);
            pane.getChildren().setAll(node);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}