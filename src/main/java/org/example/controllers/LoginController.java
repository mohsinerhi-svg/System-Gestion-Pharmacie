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

    // --- FIX: Renamed from messageLabel to errorLabel to match FXML ---
    @FXML
    private Label errorLabel;

    private UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    @FXML
    protected void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setStyle("-fx-text-fill: red;");
            errorLabel.setText("Veuillez remplir tous les champs.");
            return;
        }

        Utilisateur user = utilisateurDAO.verifierLogin(username, password);

        if (user != null) {
            errorLabel.setStyle("-fx-text-fill: green;");
            errorLabel.setText("Succès ! Redirection en cours...");

            try {
                String fxmlFile = "";
                if (user.getRole().equals("ADMIN")) {
                    fxmlFile = "/views/AdminDashboard.fxml";
                } else {
                    fxmlFile = "/views/PharmacienDashboard.fxml";
                }

                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));

                // --- BONUS UX FIX: Increased window size for dashboards so they don't look cramped ---
                Scene scene = new Scene(fxmlLoader.load(), 1280, 800);

                // Get current stage from our renamed label
                Stage stage = (Stage) errorLabel.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Tableau de bord - " + user.getRole());
                stage.centerOnScreen();
                stage.show();

            } catch (Exception e) {
                errorLabel.setStyle("-fx-text-fill: red;");
                errorLabel.setText("Erreur lors de l'ouverture du tableau de bord.");
                e.printStackTrace();
            }

        } else {
            errorLabel.setStyle("-fx-text-fill: red;");
            errorLabel.setText("Nom d'utilisateur ou mot de passe incorrect.");
        }
    }
}