package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        System.out.println("Recherche du fichier FXML...");
        java.net.URL url = Main.class.getResource("/views/Login.fxml");

        if (url == null) {
            System.err.println("CATASTROPHE : Le fichier est introuvable ! Maven ne l'a pas copié dans 'target/classes'.");
            return; // Arrête le programme avant de planter
        }

        System.out.println("FICHIER TROUVÉ : " + url);
        FXMLLoader fxmlLoader = new FXMLLoader(url);
        Scene scene = new Scene(fxmlLoader.load(), 400, 400);

        stage.setTitle("Gestion de Pharmacie");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    public static void main(String[] args) {
        // This launches the JavaFX GUI
        launch();
    }
}