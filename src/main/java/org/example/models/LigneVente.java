package org.example.models;

public class LigneVente {
    private int medicamentId;
    private String medicamentNom; // Pour l'affichage dans le tableau
    private int quantite;
    private double prixSousTotal;

    public LigneVente(int medicamentId, String medicamentNom, int quantite, double prixSousTotal) {
        this.medicamentId = medicamentId;
        this.medicamentNom = medicamentNom;
        this.quantite = quantite;
        this.prixSousTotal = prixSousTotal;
    }

    public int getMedicamentId() { return medicamentId; }
    public String getMedicamentNom() { return medicamentNom; }
    public int getQuantite() { return quantite; }
    public double getPrixSousTotal() { return prixSousTotal; }
}