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
import org.example.models.Medicament;
import org.example.models.MedicamentDAO;
import org.example.models.Utilisateur;
import org.example.models.UtilisateurDAO;
import org.example.models.VenteDAO;

import java.io.IOException;
import java.util.List;

public class AdminDashboardController {

    // --- ONGLET 1 : UTILISATEURS ---
    @FXML private TableView<Utilisateur> utilisateursTable;
    @FXML private TableColumn<Utilisateur, Integer> idCol;
    @FXML private TableColumn<Utilisateur, String> usernameCol;
    @FXML private TableColumn<Utilisateur, String> roleCol;
    @FXML private TextField newUsernameField;
    @FXML private PasswordField newPasswordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label messageLabel;

    // --- ONGLET 2 : STATISTIQUES ---
    @FXML private Label caTotalLabel;
    @FXML private TableView<Medicament> ruptureTable;
    @FXML private TableColumn<Medicament, Integer> rupIdCol;
    @FXML private TableColumn<Medicament, String> rupNomCol;
    @FXML private TableColumn<Medicament, Integer> rupStockCol;

    private UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private VenteDAO venteDAO = new VenteDAO();
    private MedicamentDAO medicamentDAO = new MedicamentDAO();

    @FXML
    public void initialize() {
        // Init Colonnes Utilisateurs
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleComboBox.setItems(FXCollections.observableArrayList("PHARMACIEN", "ADMIN"));

        // Init Colonnes Ruptures de Stock
        rupIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        rupNomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        rupStockCol.setCellValueFactory(new PropertyValueFactory<>("quantiteStock"));

        chargerUtilisateurs();
        chargerStatistiques();
    }

    private void chargerUtilisateurs() {
        List<Utilisateur> liste = utilisateurDAO.getTousLesUtilisateurs();
        utilisateursTable.setItems(FXCollections.observableArrayList(liste));
    }

    @FXML
    protected void chargerStatistiques() {
        // 1. Charger le chiffre d'affaires
        double caTotal = venteDAO.getChiffreAffairesTotal();
        caTotalLabel.setText(String.format("%.2f €", caTotal));

        // 2. Charger les médicaments en rupture (stock <= 5)
        List<Medicament> ruptures = medicamentDAO.getMedicamentsEnRupture(5);
        ruptureTable.setItems(FXCollections.observableArrayList(ruptures));
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

        if (utilisateurDAO.ajouterUtilisateur(username, password, role)) {
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Utilisateur ajouté avec succès !");
            newUsernameField.clear(); newPasswordField.clear(); roleComboBox.setValue(null);
            chargerUtilisateurs();
        } else {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Erreur. Ce nom existe peut-être déjà.");
        }
    }

    @FXML
    protected void handleLogout(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(fxmlLoader.load(), 800, 400));
            stage.setTitle("Gestion de Pharmacie - Connexion");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}