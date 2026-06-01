package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.models.Utilisateur;
import org.example.models.UtilisateurDAO;

public class LoginController {

    // These @FXML variables link directly to the fx:id attributes in your XML
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    private UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    /**
     * This method is triggered when the "Se connecter" button is clicked.
     */
    @FXML
    protected void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // 1. Check if fields are empty
        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Veuillez remplir tous les champs.");
            return;
        }

        // 2. Ask the DAO to verify the credentials in PostgreSQL
        Utilisateur user = utilisateurDAO.verifierLogin(username, password);

        // 3. Handle the result
        if (user != null) {
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Connexion réussie ! Rôle : " + user.getRole());

            // Note: Later, we will add code right here to close the login
            // window and open the main Pharmacy Dashboard window!
        } else {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Nom d'utilisateur ou mot de passe incorrect.");
        }
    }
}