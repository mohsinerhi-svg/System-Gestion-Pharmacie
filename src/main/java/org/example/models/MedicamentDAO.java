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
                // Gestion sécurisée de la date (au cas où elle serait null)
                java.sql.Date sqlDate = rs.getDate("date_expiration");
                LocalDate localDate = (sqlDate != null) ? sqlDate.toLocalDate() : null;

                liste.add(new Medicament(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("categorie"),
                        rs.getDouble("prix_unitaire"),
                        rs.getInt("quantite_stock"),
                        localDate
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }

    public boolean ajouterMedicament(String nom, String categorie, double prixUnitaire, int quantiteStock, LocalDate dateExpiration) {
        String sql = "INSERT INTO medicaments (nom, categorie, prix_unitaire, quantite_stock, date_expiration) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nom);
            pstmt.setString(2, categorie);
            pstmt.setDouble(3, prixUnitaire);
            pstmt.setInt(4, quantiteStock);

            if (dateExpiration != null) {
                pstmt.setDate(5, java.sql.Date.valueOf(dateExpiration));
            } else {
                pstmt.setNull(5, java.sql.Types.DATE);
            }

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * Récupère les médicaments dont le stock est inférieur ou égal à un seuil.
     */
    public List<Medicament> getMedicamentsEnRupture(int seuil) {
        List<Medicament> liste = new ArrayList<>();
        String sql = "SELECT * FROM medicaments WHERE quantite_stock <= ? ORDER BY quantite_stock ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, seuil);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date sqlDate = rs.getDate("date_expiration");
                    java.time.LocalDate localDate = (sqlDate != null) ? sqlDate.toLocalDate() : null;

                    liste.add(new Medicament(
                            rs.getInt("id"),
                            rs.getString("nom"),
                            rs.getString("categorie"),
                            rs.getDouble("prix_unitaire"),
                            rs.getInt("quantite_stock"),
                            localDate
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }
}