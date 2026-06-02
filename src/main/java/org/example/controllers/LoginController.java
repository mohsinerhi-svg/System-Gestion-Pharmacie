package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.models.Utilisateur;
import org.example.models.UtilisateurDAO;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    private UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    @FXML
    protected void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Veuillez remplir tous les champs.");
            return;
        }

        Utilisateur user = utilisateurDAO.verifierLogin(username, password);

        if (user != null) {
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Succès ! Redirection en cours...");

            // --- NOUVEAU CODE : CHANGEMENT DE FENÊTRE ---
            try {
                // On choisit le dashboard en fonction du rôle
                String fxmlFile = "";
                if (user.getRole().equals("ADMIN")) {
                    fxmlFile = "/views/AdminDashboard.fxml";
                } else {
                    // Nous créerons cette page plus tard
                    fxmlFile = "/views/PharmacienDashboard.fxml";
                }

                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));
                Scene scene = new Scene(fxmlLoader.load(), 600, 400); // Fenêtre plus grande pour le Dashboard

                // On récupère la fenêtre actuelle pour la remplacer
                Stage stage = (Stage) messageLabel.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Tableau de bord - " + user.getRole());
                stage.centerOnScreen();
                stage.show();

            } catch (Exception e) {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Erreur lors de l'ouverture du tableau de bord.");
                e.printStackTrace();
            }

        } else {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Nom d'utilisateur ou mot de passe incorrect.");
        }
    }
}