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
import java.util.Optional;

public class PharmacienDashboardController {

    // --- ONGLET 1 : CAISSE ---
    @FXML private ComboBox<Client> clientCombo;
    @FXML private ComboBox<Medicament> medicamentVenteCombo;
    @FXML private TextField quantiteVenteField;
    @FXML private Label venteMessageLabel;
    @FXML private TableView<LigneVente> panierTable;
    @FXML private TableColumn<LigneVente, String> panierNomCol;
    @FXML private TableColumn<LigneVente, Integer> panierQteCol;
    @FXML private TableColumn<LigneVente, Double> panierPrixCol;
    @FXML private Label totalLabel;

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
    @FXML private TextField rechercheStockField;
    @FXML private TextField reapprovQteField;
    // Hidden field to store the ID of the medicament being edited (we store it in the tag of the save button)
    private Integer medicamentEnCoursEditionId = null;

    // --- ONGLET 3 : HISTORIQUE ---
    @FXML private TableView<Vente> historiqueTable;
    @FXML private TableColumn<Vente, Integer> histIdCol;
    @FXML private TableColumn<Vente, java.sql.Timestamp> histDateCol;
    @FXML private TableColumn<Vente, Double> histTotalCol;
    @FXML private TableColumn<Vente, String> histClientCol;
    @FXML private Label pdfMessageLabel;

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
    @FXML private TextField rechercheClientField;
    @FXML private Button btnSauvegarderClient;
    private Integer clientEnCoursEditionId = null;

    private ObservableList<LigneVente> panierList = FXCollections.observableArrayList();
    private double totalVente = 0.0;

    private final VenteDAO venteDAO = new VenteDAO();
    private final MedicamentDAO medicamentDAO = new MedicamentDAO();
    private final ClientDAO clientDAO = new ClientDAO();

    @FXML
    public void initialize() {
        // Colonnes stocks
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        categorieCol.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        prixCol.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));
        stockCol.setCellValueFactory(new PropertyValueFactory<>("quantiteStock"));
        expirationCol.setCellValueFactory(new PropertyValueFactory<>("dateExpiration"));

        // Stock en rouge si critique
        stockCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) { setText(null); setStyle(""); }
                else {
                    setText(String.valueOf(item));
                    setStyle(item < 5 ? "-fx-text-fill: #dc2626; -fx-font-weight: bold;" : "-fx-text-fill: inherit;");
                }
            }
        });

        // Colonnes panier
        panierNomCol.setCellValueFactory(new PropertyValueFactory<>("medicamentNom"));
        panierQteCol.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        panierPrixCol.setCellValueFactory(new PropertyValueFactory<>("prixSousTotal"));
        panierTable.setItems(panierList);

        // Colonnes historique
        histIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        histDateCol.setCellValueFactory(new PropertyValueFactory<>("dateVente"));
        histClientCol.setCellValueFactory(new PropertyValueFactory<>("nomClient"));
        histTotalCol.setCellValueFactory(new PropertyValueFactory<>("montantTotal"));

        // Colonnes clients
        clientIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        clientNomCol.setCellValueFactory(new PropertyValueFactory<>("nomComplet"));
        clientTelCol.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        clientEmailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        chargerMedicaments();
        chargerHistoriqueVentes();
        chargerClients();
    }

    // ===================== STOCKS =====================

    private void chargerMedicaments() {
        List<Medicament> liste = medicamentDAO.getTousLesMedicaments();
        ObservableList<Medicament> data = FXCollections.observableArrayList(liste);
        medicamentsTable.setItems(data);
        medicamentVenteCombo.setItems(data);
    }

    @FXML
    protected void handleRechercheStock() {
        String terme = rechercheStockField.getText().trim();
        List<Medicament> liste = terme.isEmpty()
                ? medicamentDAO.getTousLesMedicaments()
                : medicamentDAO.rechercher(terme);
        medicamentsTable.setItems(FXCollections.observableArrayList(liste));
    }

    @FXML
    protected void handleAjouterMedicament() {
        String nom = nomField.getText().trim();
        String categorie = categorieField.getText().trim();
        LocalDate dateExpiration = dateExpirationPicker.getValue();
        try {
            double prix = Double.parseDouble(prixField.getText());
            int stock = Integer.parseInt(stockField.getText());

            boolean succes;
            if (medicamentEnCoursEditionId != null) {
                // MODE MODIFICATION
                succes = medicamentDAO.modifierMedicament(medicamentEnCoursEditionId, nom, categorie, prix, stock, dateExpiration);
                if (succes) {
                    stockMessageLabel.setStyle("-fx-text-fill: green;");
                    stockMessageLabel.setText("Médicament modifié avec succès !");
                    medicamentEnCoursEditionId = null;
                }
            } else {
                // MODE AJOUT
                succes = medicamentDAO.ajouterMedicament(nom, categorie, prix, stock, dateExpiration);
                if (succes) {
                    stockMessageLabel.setStyle("-fx-text-fill: green;");
                    stockMessageLabel.setText("Médicament ajouté !");
                }
            }
            if (succes) {
                viderFormulaireMedicament();
                chargerMedicaments();
            }
        } catch (NumberFormatException e) {
            stockMessageLabel.setStyle("-fx-text-fill: red;");
            stockMessageLabel.setText("Prix et Stock doivent être des nombres valides.");
        }
    }

    @FXML
    protected void handleModifierMedicament() {
        Medicament sel = medicamentsTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            stockMessageLabel.setStyle("-fx-text-fill: red;");
            stockMessageLabel.setText("Sélectionnez un médicament dans le tableau.");
            return;
        }
        // Pré-remplir le formulaire
        medicamentEnCoursEditionId = sel.getId();
        nomField.setText(sel.getNom());
        categorieField.setText(sel.getCategorie());
        prixField.setText(String.valueOf(sel.getPrixUnitaire()));
        stockField.setText(String.valueOf(sel.getQuantiteStock()));
        dateExpirationPicker.setValue(sel.getDateExpiration());
        stockMessageLabel.setStyle("-fx-text-fill: #2563eb;");
        stockMessageLabel.setText("Modification en cours — modifiez puis cliquez « Valider ».");
    }

    @FXML
    protected void handleSupprimerMedicament() {
        Medicament sel = medicamentsTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            stockMessageLabel.setStyle("-fx-text-fill: red;");
            stockMessageLabel.setText("Sélectionnez un médicament à supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Supprimer « " + sel.getNom() + " » ?");
        confirm.setContentText("Cette action est irréversible. Les ventes existantes ne seront pas affectées.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (medicamentDAO.supprimerMedicament(sel.getId())) {
                stockMessageLabel.setStyle("-fx-text-fill: green;");
                stockMessageLabel.setText("Médicament supprimé.");
                chargerMedicaments();
            } else {
                stockMessageLabel.setStyle("-fx-text-fill: red;");
                stockMessageLabel.setText("Impossible de supprimer (référencé dans des ventes ?).");
            }
        }
    }

    @FXML
    protected void handleReapprovisionnement() {
        Medicament sel = medicamentsTable.getSelectionModel().getSelectedItem();
        if (sel == null || reapprovQteField == null || reapprovQteField.getText().trim().isEmpty()) {
            stockMessageLabel.setStyle("-fx-text-fill: red;");
            stockMessageLabel.setText("Sélectionnez un médicament et entrez la quantité à ajouter.");
            return;
        }
        try {
            int qte = Integer.parseInt(reapprovQteField.getText().trim());
            if (qte <= 0) throw new NumberFormatException();
            if (medicamentDAO.reapprovisionner(sel.getId(), qte)) {
                stockMessageLabel.setStyle("-fx-text-fill: green;");
                stockMessageLabel.setText("Stock mis à jour : +" + qte + " unités.");
                reapprovQteField.clear();
                chargerMedicaments();
            }
        } catch (NumberFormatException e) {
            stockMessageLabel.setStyle("-fx-text-fill: red;");
            stockMessageLabel.setText("Quantité invalide.");
        }
    }

    @FXML
    protected void handleAnnulerEditionMedicament() {
        medicamentEnCoursEditionId = null;
        viderFormulaireMedicament();
        stockMessageLabel.setText("");
    }

    private void viderFormulaireMedicament() {
        nomField.clear(); categorieField.clear(); prixField.clear();
        stockField.clear(); dateExpirationPicker.setValue(null);
    }

    // ===================== CAISSE =====================

    @FXML
    protected void handleAjouterAuPanier() {
        Medicament med = medicamentVenteCombo.getValue();
        if (med == null) { venteMessageLabel.setText("Veuillez sélectionner un médicament."); return; }
        try {
            int qteDemande = Integer.parseInt(quantiteVenteField.getText());
            if (qteDemande <= 0 || qteDemande > med.getQuantiteStock()) {
                venteMessageLabel.setStyle("-fx-text-fill: red;");
                venteMessageLabel.setText("Quantité invalide ou stock insuffisant (" + med.getQuantiteStock() + " disponibles).");
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
            venteMessageLabel.setStyle("-fx-text-fill: red;");
            venteMessageLabel.setText("Quantité invalide.");
        }
    }

    @FXML
    protected void handleRetirerDuPanier() {
        LigneVente sel = panierTable.getSelectionModel().getSelectedItem();
        if (sel == null) { venteMessageLabel.setText("Sélectionnez une ligne à retirer."); return; }
        totalVente -= sel.getPrixSousTotal();
        panierList.remove(sel);
        totalLabel.setText(String.format("%.2f Dhs", totalVente));
        venteMessageLabel.setStyle("-fx-text-fill: green;");
        venteMessageLabel.setText("Ligne retirée du panier.");
    }

    @FXML
    protected void handleViderPanier() {
        panierList.clear();
        totalVente = 0.0;
        totalLabel.setText("0.00 Dhs");
        venteMessageLabel.setText("");
    }

    @FXML
    protected void handleValiderVente() {
        if (panierList.isEmpty()) {
            venteMessageLabel.setStyle("-fx-text-fill: red;");
            venteMessageLabel.setText("Le panier est vide.");
            return;
        }
        Integer clientId = (clientCombo.getValue() != null) ? clientCombo.getValue().getId() : null;
        boolean success = venteDAO.realiserVente(1, clientId, panierList, totalVente);
        if (success) {
            panierList.clear(); totalVente = 0.0;
            totalLabel.setText("0.00 Dhs");
            clientCombo.setValue(null);
            chargerMedicaments(); chargerHistoriqueVentes();
            venteMessageLabel.setStyle("-fx-text-fill: green;");
            venteMessageLabel.setText("Vente encaissée avec succès !");
        } else {
            venteMessageLabel.setStyle("-fx-text-fill: red;");
            venteMessageLabel.setText("Erreur lors de la vente (stock insuffisant ?).");
        }
    }

    // ===================== HISTORIQUE =====================

    @FXML
    protected void chargerHistoriqueVentes() {
        historiqueTable.setItems(FXCollections.observableArrayList(venteDAO.getHistoriqueVentes()));
        if (pdfMessageLabel != null) pdfMessageLabel.setText("");
    }

    @FXML
    protected void handleAnnulerVente() {
        Vente sel = historiqueTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            pdfMessageLabel.setStyle("-fx-text-fill: red;");
            pdfMessageLabel.setText("Sélectionnez une vente à annuler.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Annuler la vente");
        confirm.setHeaderText("Annuler la vente N°" + sel.getId() + " ?");
        confirm.setContentText("Les stocks seront remis à jour. Cette action est irréversible.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (venteDAO.annulerVente(sel.getId())) {
                pdfMessageLabel.setStyle("-fx-text-fill: green;");
                pdfMessageLabel.setText("Vente annulée et stocks restaurés.");
                chargerHistoriqueVentes(); chargerMedicaments();
            } else {
                pdfMessageLabel.setStyle("-fx-text-fill: red;");
                pdfMessageLabel.setText("Erreur lors de l'annulation.");
            }
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
            pdfMessageLabel.setText("Facture_N" + venteSelectionnee.getId() + ".pdf générée !");
        } else {
            pdfMessageLabel.setStyle("-fx-text-fill: red;");
            pdfMessageLabel.setText("Erreur lors de la création du PDF.");
        }
    }

    // ===================== CLIENTS =====================

    private void chargerClients() {
        List<Client> liste = clientDAO.getTousLesClients();
        ObservableList<Client> data = FXCollections.observableArrayList(liste);
        clientCombo.setItems(data);
        clientsTable.setItems(data);
    }

    @FXML
    protected void handleRechercheClient() {
        String terme = rechercheClientField.getText().trim();
        List<Client> liste = terme.isEmpty()
                ? clientDAO.getTousLesClients()
                : clientDAO.rechercher(terme);
        clientsTable.setItems(FXCollections.observableArrayList(liste));
    }

    @FXML
    protected void handleAjouterClient() {
        String nom = clientNomField.getText().trim();
        if (nom.isEmpty()) {
            clientMessageLabel.setStyle("-fx-text-fill: red;");
            clientMessageLabel.setText("Le nom est obligatoire.");
            return;
        }
        String tel = clientTelField.getText().trim();
        String email = clientEmailField.getText().trim();

        boolean succes;
        if (clientEnCoursEditionId != null) {
            succes = clientDAO.modifierClient(clientEnCoursEditionId, nom, tel, email);
            if (succes) {
                clientMessageLabel.setStyle("-fx-text-fill: green;");
                clientMessageLabel.setText("Client modifié avec succès !");
                clientEnCoursEditionId = null;
                if (btnSauvegarderClient != null) btnSauvegarderClient.setText("Enregistrer Fiche Client");
            }
        } else {
            succes = clientDAO.ajouterClient(nom, tel, email);
            if (succes) {
                clientMessageLabel.setStyle("-fx-text-fill: green;");
                clientMessageLabel.setText("Client ajouté !");
            }
        }
        if (succes) {
            clientNomField.clear(); clientTelField.clear(); clientEmailField.clear();
            chargerClients();
        } else {
            clientMessageLabel.setStyle("-fx-text-fill: red;");
            clientMessageLabel.setText("Erreur lors de l'opération.");
        }
    }

    @FXML
    protected void handleModifierClient() {
        Client sel = clientsTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            clientMessageLabel.setStyle("-fx-text-fill: red;");
            clientMessageLabel.setText("Sélectionnez un client dans le tableau.");
            return;
        }
        clientEnCoursEditionId = sel.getId();
        clientNomField.setText(sel.getNomComplet());
        clientTelField.setText(sel.getTelephone());
        clientEmailField.setText(sel.getEmail());
        if (btnSauvegarderClient != null) btnSauvegarderClient.setText("Mettre à jour");
        clientMessageLabel.setStyle("-fx-text-fill: #2563eb;");
        clientMessageLabel.setText("Modification en cours — modifiez puis cliquez « Mettre à jour ».");
    }

    @FXML
    protected void handleSupprimerClient() {
        Client sel = clientsTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            clientMessageLabel.setStyle("-fx-text-fill: red;");
            clientMessageLabel.setText("Sélectionnez un client à supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Supprimer le client « " + sel.getNomComplet() + " » ?");
        confirm.setContentText("L'historique des ventes liées passera en « Client de passage ».");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (clientDAO.supprimerClient(sel.getId())) {
                clientMessageLabel.setStyle("-fx-text-fill: green;");
                clientMessageLabel.setText("Client supprimé.");
                chargerClients();
            } else {
                clientMessageLabel.setStyle("-fx-text-fill: red;");
                clientMessageLabel.setText("Impossible de supprimer ce client.");
            }
        }
    }

    @FXML
    protected void handleAnnulerEditionClient() {
        clientEnCoursEditionId = null;
        clientNomField.clear(); clientTelField.clear(); clientEmailField.clear();
        if (btnSauvegarderClient != null) btnSauvegarderClient.setText("Enregistrer Fiche Client");
        clientMessageLabel.setText("");
    }

    // ===================== NAVIGATION =====================

    @FXML
    protected void handleLogout(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(fxmlLoader.load(), 800, 400));
            stage.centerOnScreen(); stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}