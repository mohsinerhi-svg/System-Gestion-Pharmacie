package org.example.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class VenteDAO {

    /**
     * Enregistre une vente, ses lignes, et met à jour le stock dans une seule TRANSACTION.
     * @param utilisateurId L'ID du pharmacien connecté (on mettra 1 par défaut pour le moment)
     * @param clientId L'ID du client (peut être null)
     * @param panier La liste des articles achetés
     * @param total Le montant total
     * @return true si succès, false si erreur (ex: rupture de stock)
     */
    public boolean realiserVente(int utilisateurId, Integer clientId, List<LigneVente> panier, double total) {
        Connection conn = null;

        String sqlInsertVente = "INSERT INTO ventes (utilisateur_id, client_id, montant_total) VALUES (?, ?, ?)";
        String sqlInsertLigne = "INSERT INTO lignes_vente (vente_id, medicament_id, quantite, prix_sous_total) VALUES (?, ?, ?, ?)";
        String sqlUpdateStock = "UPDATE medicaments SET quantite_stock = quantite_stock - ? WHERE id = ? AND quantite_stock >= ?";

        try {
            conn = DatabaseConnection.getConnection();
            // DESACTIVER l'auto-commit pour commencer la transaction
            conn.setAutoCommit(false);

            // 1. Créer la Vente
            int venteId = -1;
            // Statement.RETURN_GENERATED_KEYS permet de récupérer l'ID de la vente qui vient d'être créée
            try (PreparedStatement pstmtVente = conn.prepareStatement(sqlInsertVente, Statement.RETURN_GENERATED_KEYS)) {
                pstmtVente.setInt(1, utilisateurId);
                if (clientId != null) {
                    pstmtVente.setInt(2, clientId);
                } else {
                    pstmtVente.setNull(2, java.sql.Types.INTEGER);
                }
                pstmtVente.setDouble(3, total);
                pstmtVente.executeUpdate();

                try (ResultSet rs = pstmtVente.getGeneratedKeys()) {
                    if (rs.next()) {
                        venteId = rs.getInt(1);
                    }
                }
            }

            if (venteId == -1) throw new SQLException("Échec de la création de la vente.");

            // 2. Traiter chaque article du panier
            try (PreparedStatement pstmtLigne = conn.prepareStatement(sqlInsertLigne);
                 PreparedStatement pstmtStock = conn.prepareStatement(sqlUpdateStock)) {

                for (LigneVente ligne : panier) {
                    // a) Réduire le stock (et vérifier qu'il y en a assez)
                    pstmtStock.setInt(1, ligne.getQuantite());
                    pstmtStock.setInt(2, ligne.getMedicamentId());
                    pstmtStock.setInt(3, ligne.getQuantite()); // Condition : stock >= quantité demandée

                    int rowsUpdated = pstmtStock.executeUpdate();
                    if (rowsUpdated == 0) {
                        // Si 0 ligne mise à jour, c'est qu'il n'y a pas assez de stock !
                        throw new SQLException("Stock insuffisant pour le médicament ID: " + ligne.getMedicamentId());
                    }

                    // b) Ajouter la ligne de vente
                    pstmtLigne.setInt(1, venteId);
                    pstmtLigne.setInt(2, ligne.getMedicamentId());
                    pstmtLigne.setInt(3, ligne.getQuantite());
                    pstmtLigne.setDouble(4, ligne.getPrixSousTotal());
                    pstmtLigne.addBatch(); // On prépare toutes les requêtes
                }

                pstmtLigne.executeBatch(); // On exécute toutes les insertions de lignes d'un coup
            }

            // 3. Tout s'est bien passé, on valide la transaction !
            conn.commit();
            return true;

        } catch (SQLException e) {
            // EN CAS D'ERREUR (ex: pas de stock), on ANNULE TOUT !
            System.err.println("Erreur lors de la vente. Annulation de la transaction...");
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            // Remettre l'auto-commit à true pour les prochaines requêtes normales
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Récupère l'historique de toutes les ventes, des plus récentes aux plus anciennes.
     */
    public List<Vente> getHistoriqueVentes() {
        List<Vente> liste = new java.util.ArrayList<>();
        //  LEFT JOIN pour récupérer le nom du client
        String sql = "SELECT v.id, v.montant_total, v.date_vente, c.nom_complet " +
                "FROM ventes v LEFT JOIN clients c ON v.client_id = c.id " +
                "ORDER BY v.date_vente DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                liste.add(new Vente(
                        rs.getInt("id"),
                        rs.getDouble("montant_total"),
                        rs.getTimestamp("date_vente"),
                        rs.getString("nom_complet")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }

    /**
     * Calcule le chiffre d'affaires total de la pharmacie.
     */
    public double getChiffreAffairesTotal() {
        String sql = "SELECT SUM(montant_total) FROM ventes";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}