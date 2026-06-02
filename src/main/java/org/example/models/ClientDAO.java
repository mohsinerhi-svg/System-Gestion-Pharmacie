package org.example.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO {

    public List<Client> getTousLesClients() {
        List<Client> liste = new ArrayList<>();
        String sql = "SELECT * FROM clients ORDER BY nom_complet ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                liste.add(new Client(rs.getInt("id"), rs.getString("nom_complet"),
                        rs.getString("telephone"), rs.getString("email")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return liste;
    }

    public boolean ajouterClient(String nomComplet, String telephone, String email) {
        String sql = "INSERT INTO clients (nom_complet, telephone, email) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomComplet);
            pstmt.setString(2, telephone);
            pstmt.setString(3, email);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /**
     * Modifie les informations d'un client existant.
     */
    public boolean modifierClient(int id, String nomComplet, String telephone, String email) {
        String sql = "UPDATE clients SET nom_complet=?, telephone=?, email=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomComplet);
            pstmt.setString(2, telephone);
            pstmt.setString(3, email);
            pstmt.setInt(4, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /**
     * Supprime un client. Met client_id à NULL dans les ventes liées (si ON DELETE SET NULL configuré)
     * ou échoue si des ventes référencent ce client.
     */
    public boolean supprimerClient(int id) {
        String sql = "DELETE FROM clients WHERE id=?";
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
     * Recherche des clients par nom, téléphone ou email.
     */
    public List<Client> rechercher(String terme) {
        List<Client> liste = new ArrayList<>();
        String sql = "SELECT * FROM clients WHERE LOWER(nom_complet) LIKE ? OR telephone LIKE ? OR LOWER(email) LIKE ? ORDER BY nom_complet ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String pattern = "%" + terme.toLowerCase() + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, "%" + terme + "%");
            pstmt.setString(3, pattern);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    liste.add(new Client(rs.getInt("id"), rs.getString("nom_complet"),
                            rs.getString("telephone"), rs.getString("email")));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return liste;
    }
}