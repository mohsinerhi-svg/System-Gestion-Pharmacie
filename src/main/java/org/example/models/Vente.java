package org.example.models;

import java.sql.Timestamp;

public class Vente {
    private int id;
    private double montantTotal;
    private Timestamp dateVente;

    public Vente(int id, double montantTotal, Timestamp dateVente) {
        this.id = id;
        this.montantTotal = montantTotal;
        this.dateVente = dateVente;
    }

    public int getId() { return id; }
    public double getMontantTotal() { return montantTotal; }
    public Timestamp getDateVente() { return dateVente; }
}