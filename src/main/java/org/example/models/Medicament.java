package org.example.models;

import java.time.LocalDate;

public class Medicament {
    private int id;
    private String nom;
    private String categorie;
    private double prixUnitaire;
    private int quantiteStock;
    private LocalDate dateExpiration;

    public Medicament(int id, String nom, String categorie, double prixUnitaire, int quantiteStock, LocalDate dateExpiration) {
        this.id = id;
        this.nom = nom;
        this.categorie = categorie;
        this.prixUnitaire = prixUnitaire;
        this.quantiteStock = quantiteStock;
        this.dateExpiration = dateExpiration;
    }

    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getCategorie() { return categorie; }
    public double getPrixUnitaire() { return prixUnitaire; }
    public int getQuantiteStock() { return quantiteStock; }
    public LocalDate getDateExpiration() { return dateExpiration; }
    @Override
    public String toString() {
        return nom + " (" + prixUnitaire + " €) - Stock: " + quantiteStock;
    }
}