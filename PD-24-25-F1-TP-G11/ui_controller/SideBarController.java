package pt.isec.pd.trabalho_pratico.ui_controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

public class SideBarController {
    @FXML
    public Button btnHomePage;
    @FXML
    public Button btnGroupPage;
    @FXML
    public Button btnAddExpense;
    @FXML
    public Button btnHistoryPage;
    @FXML
    public Button btnAccountPage;
    @FXML
    private Pane ContentPane;

    public void initialize() {
        onHomePage();
    }

    public void onHomePage(){
        loadFXML("/resources/fxml/HomePage.fxml");
    }
    public void onGroupPage(){
        loadFXML("/resources/fxml/GroupPage.fxml");
    }
    public void onExpensePage(){
        loadFXML("/resources/fxml/AddExpensePage.fxml");
    }
    public void onHistoryPage(){
        loadFXML("/resources/fxml/HistoryPage.fxml");
    }
    public void onAccountPage(){
        loadFXML("/resources/fxml/AccountEditPage.fxml");
    }

    private void loadFXML(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            Node node = loader.load();
            switch (fxmlFileName){
                case "/resources/fxml/HomePage.fxml":
                    putBtnActive(btnHomePage);
                    break;
                case "/resources/fxml/GroupPage.fxml":
                    putBtnActive(btnGroupPage);
                    break;
                case "/resources/fxml/AddExpensePage.fxml":
                    putBtnActive(btnAddExpense);
                    break;
                case "/resources/fxml/HistoryPage.fxml":
                    putBtnActive(btnHistoryPage);
                    break;
                case "/resources/fxml/AccountEditPage.fxml":
                    putBtnActive(btnAccountPage);
                    break;
            }
            ContentPane.getChildren().setAll(node);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void putBtnActive(Button button) {
        btnHomePage.setStyle("-fx-background-color: white;");
        btnGroupPage.setStyle("-fx-background-color: white;");
        btnHistoryPage.setStyle("-fx-background-color: white;");
        btnAccountPage.setStyle("-fx-background-color: white;");

        if(!button.equals(btnAddExpense))
            button.setStyle("-fx-background-color: #e0fff8;"); //voltar ao default qnd os outros n estao ativos
    }
}


