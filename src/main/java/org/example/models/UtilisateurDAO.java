package org.example.models;

import at.favre.lib.crypto.bcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDAO {

    public Utilisateur verifierLogin(String username, String passwordFourni) {
        String sql = "SELECT id, username, password, role FROM utilisateurs WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String hashEnBase = rs.getString("password");
                BCrypt.Result result = BCrypt.verifyer().verify(passwordFourni.toCharArray(), hashEnBase);
                if (result.verified) {
                    return new Utilisateur(rs.getInt("id"), rs.getString("username"), rs.getString("role"));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Utilisateur> getTousLesUtilisateurs() {
        List<Utilisateur> liste = new ArrayList<>();
        String sql = "SELECT id, username, role FROM utilisateurs ORDER BY username ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                liste.add(new Utilisateur(rs.getInt("id"), rs.getString("username"), rs.getString("role")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return liste;
    }

    public boolean ajouterUtilisateur(String username, String passwordEnClair, String role) {
        String sql = "INSERT INTO utilisateurs (username, password, role) VALUES (?, ?, ?)";
        String hash = BCrypt.withDefaults().hashToString(10, passwordEnClair.toCharArray());
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hash);
            pstmt.setString(3, role);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /**
     * Supprime un utilisateur par son ID.
     * Attention : ne pas permettre la suppression du dernier ADMIN.
     */
    public boolean supprimerUtilisateur(int id) {
        String sql = "DELETE FROM utilisateurs WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /**
     * Réinitialise le mot de passe d'un utilisateur (hashé automatiquement).
     */
    public boolean reinitialiserMotDePasse(int id, String nouveauMotDePasse) {
        String sql = "UPDATE utilisateurs SET password=? WHERE id=?";
        String hash = BCrypt.withDefaults().hashToString(10, nouveauMotDePasse.toCharArray());
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hash);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /**
     * Change le rôle d'un utilisateur.
     */
    public boolean changerRole(int id, String nouveauRole) {
        String sql = "UPDATE utilisateurs SET role=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nouveauRole);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /**
     * Compte le nombre d'admins (pour éviter de supprimer le dernier).
     */
    public int compterAdmins() {
        String sql = "SELECT COUNT(*) FROM utilisateurs WHERE role='ADMIN'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}