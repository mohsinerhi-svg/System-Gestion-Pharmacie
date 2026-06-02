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
import org.example.models.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class PharmacienDashboardController {

    // --- ONGLET 1 : VENTES ET CAISSE ---
    @FXML private ComboBox<Medicament> medicamentVenteCombo;
    @FXML private TextField quantiteVenteField;
    @FXML private Label venteMessageLabel;

    @FXML private TableView<LigneVente> panierTable;
    @FXML private TableColumn<LigneVente, String> panierNomCol;
    @FXML private TableColumn<LigneVente, Integer> panierQteCol;
    @FXML private TableColumn<LigneVente, Double> panierPrixCol;
    @FXML private Label totalLabel;

    // --- ONGLET 3 : HISTORIQUE VENTES ---
    @FXML private TableView<Vente> historiqueTable;
    @FXML private TableColumn<Vente, Integer> histIdCol;
    @FXML private TableColumn<Vente, java.sql.Timestamp> histDateCol;
    @FXML private TableColumn<Vente, Double> histTotalCol;

    private ObservableList<LigneVente> panierList = FXCollections.observableArrayList();
    private double totalVente = 0.0;
    private VenteDAO venteDAO = new VenteDAO();

    // --- ONGLET 2 : STOCKS ---
    @FXML private TableView<Medicament> medicamentsTable;
    @FXML private TableColumn<Medicament, Integer> idCol;
    @FXML private TableColumn<Medicament, String> nomCol;
    @FXML private TableColumn<Medicament, String> categorieCol;
    @FXML private TableColumn<Medicament, Double> prixCol;
    @FXML private TableColumn<Medicament, Integer> stockCol;
    @FXML private TableColumn<Medicament, LocalDate> expirationCol;

    @FXML private TextField nomField;
    @FXML private TextField categorieField;
    @FXML private TextField prixField;
    @FXML private TextField stockField;
    @FXML private DatePicker dateExpirationPicker;
    @FXML private Label stockMessageLabel;

    private MedicamentDAO medicamentDAO = new MedicamentDAO();

    @FXML
    public void initialize() {
        // Init Colonnes Stock
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        categorieCol.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        prixCol.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));
        stockCol.setCellValueFactory(new PropertyValueFactory<>("quantiteStock"));
        expirationCol.setCellValueFactory(new PropertyValueFactory<>("dateExpiration"));

        // Init Colonnes Panier
        panierNomCol.setCellValueFactory(new PropertyValueFactory<>("medicamentNom"));
        panierQteCol.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        panierPrixCol.setCellValueFactory(new PropertyValueFactory<>("prixSousTotal"));
        panierTable.setItems(panierList);

        // Init Colonnes Historique
        histIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        histDateCol.setCellValueFactory(new PropertyValueFactory<>("dateVente"));
        histTotalCol.setCellValueFactory(new PropertyValueFactory<>("montantTotal"));
        chargerHistoriqueVentes(); // Charge les données au démarrage

        chargerMedicaments();
    }


    private void chargerMedicaments() {
        List<Medicament> liste = medicamentDAO.getTousLesMedicaments();
        ObservableList<Medicament> data = FXCollections.observableArrayList(liste);

        // On met à jour le tableau des stocks
        medicamentsTable.setItems(data);
        // On met à jour la liste déroulante des ventes !
        medicamentVenteCombo.setItems(data);
    }

    // =========================================================
    // LOGIQUE DE L'ONGLET VENTE
    // =========================================================

    @FXML
    protected void handleAjouterAuPanier() {
        Medicament med = medicamentVenteCombo.getValue();

        if (med == null) {
            venteMessageLabel.setStyle("-fx-text-fill: red;");
            venteMessageLabel.setText("Veuillez sélectionner un médicament.");
            return;
        }

        try {
            int qteDemande = Integer.parseInt(quantiteVenteField.getText());

            if (qteDemande <= 0) {
                venteMessageLabel.setText("La quantité doit être supérieure à 0.");
                return;
            }
            if (qteDemande > med.getQuantiteStock()) {
                venteMessageLabel.setText("Stock insuffisant ! Il ne reste que " + med.getQuantiteStock() + " en stock.");
                return;
            }

            // Calcul du sous-total
            double sousTotal = qteDemande * med.getPrixUnitaire();

            // Ajouter au panier
            LigneVente ligne = new LigneVente(med.getId(), med.getNom(), qteDemande, sousTotal);
            panierList.add(ligne);

            // Mettre à jour le total général
            totalVente += sousTotal;
            totalLabel.setText(String.format("%.2f Dhs", totalVente));

            venteMessageLabel.setStyle("-fx-text-fill: green;");
            venteMessageLabel.setText("Ajouté au panier !");
            quantiteVenteField.clear();

        } catch (NumberFormatException e) {
            venteMessageLabel.setStyle("-fx-text-fill: red;");
            venteMessageLabel.setText("Quantité invalide.");
        }
    }

    @FXML
    protected void handleValiderVente() {
        if (panierList.isEmpty()) {
            venteMessageLabel.setStyle("-fx-text-fill: red;");
            venteMessageLabel.setText("Le panier est vide !");
            return;
        }

        // 1 est l'ID de l'utilisateur par défaut. Plus tard on pourra récupérer l'ID réel connecté.
        boolean success = venteDAO.realiserVente(1, null, panierList, totalVente);

        if (success) {
            // Vider le panier
            panierList.clear();
            totalVente = 0.0;
            totalLabel.setText("0.00 Dhs");

            // Rafraîchir les stocks dans toute l'application
            chargerMedicaments();

            // ---> LIGNE MANQUANTE AJOUTÉE ICI <---
            chargerHistoriqueVentes(); // Met à jour le tableau de l'historique !

            venteMessageLabel.setStyle("-fx-text-fill: green;");
            venteMessageLabel.setText("Vente validée avec succès ! Le stock a été mis à jour.");
        } else {
            venteMessageLabel.setStyle("-fx-text-fill: red;");
            venteMessageLabel.setText("Erreur lors de la vente. Vérifiez les stocks.");
        }
    }

    // =========================================================
    // LOGIQUE DE L'ONGLET STOCK (Même code qu'avant)
    // =========================================================

    @FXML
    protected void handleAjouterMedicament() {
        String nom = nomField.getText();
        String categorie = categorieField.getText();
        LocalDate dateExpiration = dateExpirationPicker.getValue();

        try {
            double prixUnitaire = Double.parseDouble(prixField.getText());
            int quantiteStock = Integer.parseInt(stockField.getText());

            if (nom.isEmpty()) {
                stockMessageLabel.setText("Le nom est obligatoire.");
                return;
            }

            if (medicamentDAO.ajouterMedicament(nom, categorie, prixUnitaire, quantiteStock, dateExpiration)) {
                stockMessageLabel.setStyle("-fx-text-fill: green;");
                stockMessageLabel.setText("Médicament ajouté !");
                nomField.clear(); categorieField.clear(); prixField.clear(); stockField.clear(); dateExpirationPicker.setValue(null);
                chargerMedicaments();
            } else {
                stockMessageLabel.setStyle("-fx-text-fill: red;");
                stockMessageLabel.setText("Erreur lors de l'ajout.");
            }
        } catch (NumberFormatException e) {
            stockMessageLabel.setStyle("-fx-text-fill: red;");
            stockMessageLabel.setText("Prix et Stock invalides.");
        }
    }

    // =========================================================
    // DECONNEXION
    // =========================================================

    @FXML
    protected void handleLogout(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/Login.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 800, 400);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Gestion de Pharmacie - Connexion");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void chargerHistoriqueVentes() {
        List<Vente> listeVentes = venteDAO.getHistoriqueVentes();
        ObservableList<Vente> dataVentes = FXCollections.observableArrayList(listeVentes);
        historiqueTable.setItems(dataVentes);
    }
}