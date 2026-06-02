package org.example.models;

import at.favre.lib.crypto.bcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import at.favre.lib.crypto.bcrypt.BCrypt;


public class UtilisateurDAO {

    /**
     * Vérifie les identifiants de l'utilisateur.
     * @param username Le nom d'utilisateur tapé
     * @param passwordFourni Le mot de passe tapé (en texte clair)
     * @return Un objet Utilisateur si le login réussit, null si ça échoue.
     */
    public Utilisateur verifierLogin(String username, String passwordFourni) {
        // La requête SQL pour chercher l'utilisateur par son nom
        String sql = "SELECT id, username, password, role FROM utilisateurs WHERE username = ?";

        // On récupère la connexion depuis notre Singleton
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            // Si l'utilisateur existe dans la base de données...
            if (rs.next()) {
                String hashEnBase = rs.getString("password");

                // On utilise BCrypt pour comparer le mot de passe tapé avec le hash de la BDD
                BCrypt.Result result = BCrypt.verifyer().verify(passwordFourni.toCharArray(), hashEnBase);
                if (result.verified) {
                    System.out.println("Login réussi pour : " + username);
                    // On retourne l'utilisateur pour que l'application sache qui est connecté
                    return new Utilisateur(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("role")
                    );
                } else {
                    System.out.println("Mot de passe incorrect.");
                }
            } else {
                System.out.println("Utilisateur introuvable.");
            }

        } catch (SQLException e) {
            System.err.println("Erreur SQL lors du login.");
            e.printStackTrace();
        }

        // Si on arrive ici, c'est que le login a échoué
        return null;
    }

    public List<Utilisateur> getTousLesUtilisateurs() {
        List<Utilisateur> liste = new ArrayList<>();
        String sql = "SELECT id, username, role FROM utilisateurs";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                liste.add(new Utilisateur(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("role")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des utilisateurs.");
            e.printStackTrace();
        }
        return liste;
    }

    /**
     * Ajoute un nouvel utilisateur avec un mot de passe sécurisé (hashé).
     */
    public boolean ajouterUtilisateur(String username, String passwordEnClair, String role) {
        String sql = "INSERT INTO utilisateurs (username, password, role) VALUES (?, ?, ?)";

        // On hache le mot de passe avant de le sauvegarder
        String hash = BCrypt.withDefaults().hashToString(10, passwordEnClair.toCharArray());

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, hash);
            pstmt.setString(3, role);

            int lignesModifiees = pstmt.executeUpdate();
            return lignesModifiees > 0; // Retourne true si l'insertion a réussi

        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de l'utilisateur.");
            e.printStackTrace();
            return false;
        }
    }
}