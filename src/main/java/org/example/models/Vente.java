package org.example.models;
import java.sql.Timestamp;

public class Vente {
    private int id;
    private double montantTotal;
    private Timestamp dateVente;
    private String nomClient; // <-- NOUVEAU

    public Vente(int id, double montantTotal, Timestamp dateVente, String nomClient) {
        this.id = id;
        this.montantTotal = montantTotal;
        this.dateVente = dateVente;
        this.nomClient = nomClient;
    }

    public int getId() { return id; }
    public double getMontantTotal() { return montantTotal; }
    public Timestamp getDateVente() { return dateVente; }
    public String getNomClient() { return (nomClient != null) ? nomClient : "Client de passage"; }
}