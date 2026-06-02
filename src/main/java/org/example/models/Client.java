package org.example.models;

public class Client {
    private int id;
    private String nomComplet;
    private String telephone;
    private String email;

    public Client(int id, String nomComplet, String telephone, String email) {
        this.id = id;
        this.nomComplet = nomComplet;
        this.telephone = telephone;
        this.email = email;
    }

    public int getId() { return id; }
    public String getNomComplet() { return nomComplet; }
    public String getTelephone() { return telephone; }
    public String getEmail() { return email; }

    @Override
    public String toString() {
        return nomComplet; // Utile pour afficher le nom dans la liste déroulante (ComboBox)
    }
}