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
                liste.add(new Client(
                        rs.getInt("id"),
                        rs.getString("nom_complet"),
                        rs.getString("telephone"),
                        rs.getString("email")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }

    /**
     * Ajoute un nouveau client dans la base de données.
     */
    public boolean ajouterClient(String nomComplet, String telephone, String email) {
        String sql = "INSERT INTO clients (nom_complet, telephone, email) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nomComplet);
            pstmt.setString(2, telephone);
            pstmt.setString(3, email);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}