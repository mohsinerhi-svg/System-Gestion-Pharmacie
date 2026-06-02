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
    @FXML private ComboBox<Client> clientCombo;
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
    @FXML private Label pdfMessageLabel;

    @FXML private TableColumn<Vente, String> histClientCol;

    private ObservableList<LigneVente> panierList = FXCollections.observableArrayList();
    private double totalVente = 0.0;

    private VenteDAO venteDAO = new VenteDAO();
    private MedicamentDAO medicamentDAO = new MedicamentDAO();
    private ClientDAO clientDAO = new ClientDAO();

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

    // --- ONGLET 4 : CLIENTS ---
    @FXML private TextField clientNomField;
    @FXML private TextField clientTelField;
    @FXML private TextField clientEmailField;
    @FXML private Label clientMessageLabel;
    @FXML private TableView<Client> clientsTable;
    @FXML private TableColumn<Client, Integer> clientIdCol;
    @FXML private TableColumn<Client, String> clientNomCol;
    @FXML private TableColumn<Client, String> clientTelCol;
    @FXML private TableColumn<Client, String> clientEmailCol;

    @FXML
    public void initialize() {
        // Init Colonnes Stock
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        categorieCol.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        prixCol.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));
        stockCol.setCellValueFactory(new PropertyValueFactory<>("quantiteStock"));
        expirationCol.setCellValueFactory(new PropertyValueFactory<>("dateExpiration"));

        // --- LA MAGIE POUR METTRE LE STOCK EN ROUGE SI < 5 ---
        stockCol.setCellFactory(column -> {
            return new TableCell<Medicament, Integer>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(String.valueOf(item));
                        if (item < 5) {
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold;"); // Alerte visuelle
                        } else {
                            setStyle("-fx-text-fill: black;");
                        }
                    }
                }
            };
        });

        // Init Colonnes Panier
        panierNomCol.setCellValueFactory(new PropertyValueFactory<>("medicamentNom"));
        panierQteCol.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        panierPrixCol.setCellValueFactory(new PropertyValueFactory<>("prixSousTotal"));
        panierTable.setItems(panierList);

        // Init Colonnes Historique
        histIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        histDateCol.setCellValueFactory(new PropertyValueFactory<>("dateVente"));
        histClientCol.setCellValueFactory(new PropertyValueFactory<>("nomClient")); // <-- NOUVEAU
        histTotalCol.setCellValueFactory(new PropertyValueFactory<>("montantTotal"));

        // Init Colonnes Clients (si vous avez l'onglet 4)
        clientIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        clientNomCol.setCellValueFactory(new PropertyValueFactory<>("nomComplet"));
        clientTelCol.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        clientEmailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        chargerMedicaments();
        chargerHistoriqueVentes();
        chargerClients();
    }

    private void chargerMedicaments() {
        List<Medicament> liste = medicamentDAO.getTousLesMedicaments();
        ObservableList<Medicament> data = FXCollections.observableArrayList(liste);
        medicamentsTable.setItems(data);
        medicamentVenteCombo.setItems(data);
    }

    private void chargerClients() {
        List<Client> liste = clientDAO.getTousLesClients();
        ObservableList<Client> data = FXCollections.observableArrayList(liste);

        // Met à jour la liste déroulante dans l'onglet Caisse
        clientCombo.setItems(data);

        // Met à jour le tableau dans le nouvel onglet Clients
        clientsTable.setItems(data);
    }

    @FXML
    protected void chargerHistoriqueVentes() {
        List<Vente> listeVentes = venteDAO.getHistoriqueVentes();
        historiqueTable.setItems(FXCollections.observableArrayList(listeVentes));
        pdfMessageLabel.setText(""); // Effacer le message PDF au refresh
    }

    @FXML
    protected void handleAjouterClient() {
        String nom = clientNomField.getText();
        String tel = clientTelField.getText();
        String email = clientEmailField.getText();

        if (nom.isEmpty()) {
            clientMessageLabel.setStyle("-fx-text-fill: red;");
            clientMessageLabel.setText("Le nom est obligatoire.");
            return;
        }

        if (clientDAO.ajouterClient(nom, tel, email)) {
            clientMessageLabel.setStyle("-fx-text-fill: green;");
            clientMessageLabel.setText("Client ajouté !");
            clientNomField.clear(); clientTelField.clear(); clientEmailField.clear();

            // Cette ligne magique met à jour le tableau ET la liste déroulante de la caisse !
            chargerClients();
        } else {
            clientMessageLabel.setStyle("-fx-text-fill: red;");
            clientMessageLabel.setText("Erreur lors de l'ajout.");
        }
    }

    @FXML
    protected void handleAjouterAuPanier() {
        Medicament med = medicamentVenteCombo.getValue();
        if (med == null) {
            venteMessageLabel.setText("Veuillez sélectionner un médicament.");
            return;
        }
        try {
            int qteDemande = Integer.parseInt(quantiteVenteField.getText());
            if (qteDemande <= 0 || qteDemande > med.getQuantiteStock()) {
                venteMessageLabel.setText("Quantité invalide ou stock insuffisant !");
                return;
            }
            double sousTotal = qteDemande * med.getPrixUnitaire();
            panierList.add(new LigneVente(med.getId(), med.getNom(), qteDemande, sousTotal));
            totalVente += sousTotal;
            totalLabel.setText(String.format("%.2f Dhs", totalVente));
            venteMessageLabel.setStyle("-fx-text-fill: green;");
            venteMessageLabel.setText("Ajouté au panier !");
            quantiteVenteField.clear();
        } catch (NumberFormatException e) {
            venteMessageLabel.setText("Quantité invalide.");
        }
    }

    @FXML
    protected void handleValiderVente() {
        if (panierList.isEmpty()) return;

        // Récupérer l'ID du client sélectionné, ou null s'il n'y en a pas
        Integer clientId = null;
        if (clientCombo.getValue() != null) {
            clientId = clientCombo.getValue().getId();
        }

        boolean success = venteDAO.realiserVente(1, clientId, panierList, totalVente);

        if (success) {
            panierList.clear();
            totalVente = 0.0;
            totalLabel.setText("0.00 Dhs");
            clientCombo.setValue(null); // Réinitialiser le client

            chargerMedicaments();
            chargerHistoriqueVentes();
            venteMessageLabel.setStyle("-fx-text-fill: green;");
            venteMessageLabel.setText("Vente validée !");
        } else {
            venteMessageLabel.setStyle("-fx-text-fill: red;");
            venteMessageLabel.setText("Erreur lors de la vente.");
        }
    }

    @FXML
    protected void handleImprimerFacture() {
        Vente venteSelectionnee = historiqueTable.getSelectionModel().getSelectedItem();
        if (venteSelectionnee == null) {
            pdfMessageLabel.setStyle("-fx-text-fill: red;");
            pdfMessageLabel.setText("Sélectionnez une vente dans le tableau !");
            return;
        }

        if (GenerateurPDF.genererFacture(venteSelectionnee)) {
            pdfMessageLabel.setStyle("-fx-text-fill: green;");
            pdfMessageLabel.setText("Facture générée dans le dossier de votre projet !");
        } else {
            pdfMessageLabel.setStyle("-fx-text-fill: red;");
            pdfMessageLabel.setText("Erreur lors de la création du PDF.");
        }
    }

    @FXML
    protected void handleAjouterMedicament() {
        String nom = nomField.getText();
        String categorie = categorieField.getText();
        LocalDate dateExpiration = dateExpirationPicker.getValue();
        try {
            if (medicamentDAO.ajouterMedicament(nom, categorie, Double.parseDouble(prixField.getText()), Integer.parseInt(stockField.getText()), dateExpiration)) {
                stockMessageLabel.setStyle("-fx-text-fill: green;");
                stockMessageLabel.setText("Médicament ajouté !");
                nomField.clear(); categorieField.clear(); prixField.clear(); stockField.clear(); dateExpirationPicker.setValue(null);
                chargerMedicaments();
            }
        } catch (NumberFormatException e) {
            stockMessageLabel.setText("Prix et Stock invalides.");
        }
    }

    @FXML
    protected void handleLogout(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(fxmlLoader.load(), 800, 400));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}