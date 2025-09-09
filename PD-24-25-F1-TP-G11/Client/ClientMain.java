package pt.isec.pd.trabalho_pratico.Client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientMain extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        try {
            if (System.getProperty("os.name").contains("Windows")) new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            else new ProcessBuilder("clear").inheritIO().start().waitFor();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/FirstPage.fxml"));
        Scene scene = new Scene(loader.load());

        stage.setScene(scene);
        stage.getIcons().add(new Image(String.valueOf(getClass().getResource("/resources/images/icon.png"))));
        stage.setTitle("Divisão de Despesas");
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        if(args.length < 2)
            System.out.printf("Usage: java client port");
        else if(args[0].equals("client"))
        {
            int port = Integer.parseInt(args[1]);
            Client.getInstance().setPort(port);
            launch(args);
        }

    }
}