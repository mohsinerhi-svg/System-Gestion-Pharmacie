package org.example.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MedicamentDAO {

    public List<Medicament> getTousLesMedicaments() {
        List<Medicament> liste = new ArrayList<>();
        String sql = "SELECT * FROM medicaments ORDER BY nom ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                java.sql.Date sqlDate = rs.getDate("date_expiration");
                LocalDate localDate = (sqlDate != null) ? sqlDate.toLocalDate() : null;
                liste.add(new Medicament(
                        rs.getInt("id"), rs.getString("nom"),
                        rs.getString("categorie"), rs.getDouble("prix_unitaire"),
                        rs.getInt("quantite_stock"), localDate));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return liste;
    }

    public boolean ajouterMedicament(String nom, String categorie, double prixUnitaire,
                                     int quantiteStock, LocalDate dateExpiration) {
        String sql = "INSERT INTO medicaments (nom, categorie, prix_unitaire, quantite_stock, date_expiration) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nom);
            pstmt.setString(2, categorie);
            pstmt.setDouble(3, prixUnitaire);
            pstmt.setInt(4, quantiteStock);
            if (dateExpiration != null)
                pstmt.setDate(5, java.sql.Date.valueOf(dateExpiration));
            else
                pstmt.setNull(5, java.sql.Types.DATE);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /**
     * Met à jour toutes les informations d'un médicament existant.
     */
    public boolean modifierMedicament(int id, String nom, String categorie, double prixUnitaire,
                                      int quantiteStock, LocalDate dateExpiration) {
        String sql = "UPDATE medicaments SET nom=?, categorie=?, prix_unitaire=?, quantite_stock=?, date_expiration=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nom);
            pstmt.setString(2, categorie);
            pstmt.setDouble(3, prixUnitaire);
            pstmt.setInt(4, quantiteStock);
            if (dateExpiration != null)
                pstmt.setDate(5, java.sql.Date.valueOf(dateExpiration));
            else
                pstmt.setNull(5, java.sql.Types.DATE);
            pstmt.setInt(6, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /**
     * Supprime un médicament. Échoue si des lignes de vente y font référence.
     */
    public boolean supprimerMedicament(int id) {
        String sql = "DELETE FROM medicaments WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Incrémente le stock d'un médicament existant (réapprovisionnement).
     */
    public boolean reapprovisionner(int id, int quantiteAAjouter) {
        String sql = "UPDATE medicaments SET quantite_stock = quantite_stock + ? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantiteAAjouter);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<Medicament> getMedicamentsEnRupture(int seuil) {
        List<Medicament> liste = new ArrayList<>();
        String sql = "SELECT * FROM medicaments WHERE quantite_stock <= ? ORDER BY quantite_stock ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, seuil);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date sqlDate = rs.getDate("date_expiration");
                    LocalDate localDate = (sqlDate != null) ? sqlDate.toLocalDate() : null;
                    liste.add(new Medicament(rs.getInt("id"), rs.getString("nom"),
                            rs.getString("categorie"), rs.getDouble("prix_unitaire"),
                            rs.getInt("quantite_stock"), localDate));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return liste;
    }

    /**
     * Recherche des médicaments par nom ou catégorie (insensible à la casse).
     */
    public List<Medicament> rechercher(String terme) {
        List<Medicament> liste = new ArrayList<>();
        String sql = "SELECT * FROM medicaments WHERE LOWER(nom) LIKE ? OR LOWER(categorie) LIKE ? ORDER BY nom ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String pattern = "%" + terme.toLowerCase() + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date sqlDate = rs.getDate("date_expiration");
                    LocalDate localDate = (sqlDate != null) ? sqlDate.toLocalDate() : null;
                    liste.add(new Medicament(rs.getInt("id"), rs.getString("nom"),
                            rs.getString("categorie"), rs.getDouble("prix_unitaire"),
                            rs.getInt("quantite_stock"), localDate));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return liste;
    }
}