package org.example.models;

/**
 * Modèle représentant un utilisateur du système (Pharmacien ou Admin)
 */
public class Utilisateur {
    private int id;
    private String username;
    private String role;

    // Constructeur
    public Utilisateur(int id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
}