package org.example.models;

import java.sql.*;
import java.util.List;

public class VenteDAO {

    public boolean realiserVente(int utilisateurId, Integer clientId, List<LigneVente> panier, double total) {
        Connection conn = null;
        String sqlInsertVente = "INSERT INTO ventes (utilisateur_id, client_id, montant_total) VALUES (?, ?, ?)";
        String sqlInsertLigne = "INSERT INTO lignes_vente (vente_id, medicament_id, quantite, prix_sous_total) VALUES (?, ?, ?, ?)";
        String sqlUpdateStock = "UPDATE medicaments SET quantite_stock = quantite_stock - ? WHERE id = ? AND quantite_stock >= ?";

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            int venteId = -1;
            try (PreparedStatement pstmtVente = conn.prepareStatement(sqlInsertVente, Statement.RETURN_GENERATED_KEYS)) {
                pstmtVente.setInt(1, utilisateurId);
                if (clientId != null) pstmtVente.setInt(2, clientId);
                else pstmtVente.setNull(2, Types.INTEGER);
                pstmtVente.setDouble(3, total);
                pstmtVente.executeUpdate();
                try (ResultSet rs = pstmtVente.getGeneratedKeys()) {
                    if (rs.next()) venteId = rs.getInt(1);
                }
            }

            if (venteId == -1) throw new SQLException("Échec de la création de la vente.");

            try (PreparedStatement pstmtLigne = conn.prepareStatement(sqlInsertLigne);
                 PreparedStatement pstmtStock = conn.prepareStatement(sqlUpdateStock)) {
                for (LigneVente ligne : panier) {
                    pstmtStock.setInt(1, ligne.getQuantite());
                    pstmtStock.setInt(2, ligne.getMedicamentId());
                    pstmtStock.setInt(3, ligne.getQuantite());
                    int rowsUpdated = pstmtStock.executeUpdate();
                    if (rowsUpdated == 0)
                        throw new SQLException("Stock insuffisant pour le médicament ID: " + ligne.getMedicamentId());
                    pstmtLigne.setInt(1, venteId);
                    pstmtLigne.setInt(2, ligne.getMedicamentId());
                    pstmtLigne.setInt(3, ligne.getQuantite());
                    pstmtLigne.setDouble(4, ligne.getPrixSousTotal());
                    pstmtLigne.addBatch();
                }
                pstmtLigne.executeBatch();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } }
            return false;
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); } }
        }
    }

    /**
     * Supprime une vente et ses lignes, et REMET les stocks à jour (annulation de vente).
     */
    public boolean annulerVente(int venteId) {
        Connection conn = null;
        String sqlRestoreStock = "UPDATE medicaments SET quantite_stock = quantite_stock + lv.quantite FROM lignes_vente lv WHERE medicaments.id = lv.medicament_id AND lv.vente_id = ?";
        String sqlDeleteLignes = "DELETE FROM lignes_vente WHERE vente_id=?";
        String sqlDeleteVente  = "DELETE FROM ventes WHERE id=?";

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Remettre le stock
            try (PreparedStatement pstmt = conn.prepareStatement(sqlRestoreStock)) {
                pstmt.setInt(1, venteId);
                pstmt.executeUpdate();
            }
            // 2. Supprimer les lignes
            try (PreparedStatement pstmt = conn.prepareStatement(sqlDeleteLignes)) {
                pstmt.setInt(1, venteId);
                pstmt.executeUpdate();
            }
            // 3. Supprimer la vente
            try (PreparedStatement pstmt = conn.prepareStatement(sqlDeleteVente)) {
                pstmt.setInt(1, venteId);
                pstmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } }
            return false;
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); } }
        }
    }

    public List<Vente> getHistoriqueVentes() {
        List<Vente> liste = new java.util.ArrayList<>();
        String sql = "SELECT v.id, v.montant_total, v.date_vente, c.nom_complet " +
                "FROM ventes v LEFT JOIN clients c ON v.client_id = c.id " +
                "ORDER BY v.date_vente DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                liste.add(new Vente(rs.getInt("id"), rs.getDouble("montant_total"),
                        rs.getTimestamp("date_vente"), rs.getString("nom_complet")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return liste;
    }

    public double getChiffreAffairesTotal() {
        String sql = "SELECT SUM(montant_total) FROM ventes";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }
}