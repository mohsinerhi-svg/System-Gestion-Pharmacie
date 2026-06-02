package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.models.Utilisateur;
import org.example.models.UtilisateurDAO;

import java.io.IOException;
import java.util.List;

public class AdminDashboardController {

    @FXML private TableView<Utilisateur> utilisateursTable;
    @FXML private TableColumn<Utilisateur, Integer> idCol;
    @FXML private TableColumn<Utilisateur, String> usernameCol;
    @FXML private TableColumn<Utilisateur, String> roleCol;

    @FXML private TextField newUsernameField;
    @FXML private PasswordField newPasswordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label messageLabel;

    private UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private ObservableList<Utilisateur> utilisateursList;

    /**
     * Cette méthode est appelée automatiquement par JavaFX quand la page s'ouvre.
     */
    @FXML
    public void initialize() {
        // Configurer les colonnes du tableau pour qu'elles lisent les attributs de la classe Utilisateur
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Remplir la liste déroulante des rôles
        roleComboBox.setItems(FXCollections.observableArrayList("PHARMACIEN", "ADMIN"));

        // Charger les données depuis PostgreSQL
        chargerUtilisateurs();
    }

    private void chargerUtilisateurs() {
        List<Utilisateur> liste = utilisateurDAO.getTousLesUtilisateurs();
        utilisateursList = FXCollections.observableArrayList(liste);
        utilisateursTable.setItems(utilisateursList);
    }

    @FXML
    protected void handleAjouterUtilisateur() {
        String username = newUsernameField.getText();
        String password = newPasswordField.getText();
        String role = roleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Veuillez remplir tous les champs.");
            return;
        }

        boolean succes = utilisateurDAO.ajouterUtilisateur(username, password, role);

        if (succes) {
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Utilisateur ajouté avec succès !");

            // Vider les champs
            newUsernameField.clear();
            newPasswordField.clear();
            roleComboBox.setValue(null);

            // Rafraîchir le tableau
            chargerUtilisateurs();
        } else {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Erreur lors de l'ajout. Le nom d'utilisateur existe peut-être déjà.");
        }
    }

    @FXML
    protected void handleLogout(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/Login.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 400, 400);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Gestion de Pharmacie - Connexion");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}