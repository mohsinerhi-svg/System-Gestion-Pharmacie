package org.example.models;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;

public class GenerateurPDF {

    public static boolean genererFacture(Vente vente) {
        try {
            String nomFichier = "Facture_N" + vente.getId() + ".pdf";
            Document document = new Document(PageSize.A5); // Format A5 style ticket de caisse
            PdfWriter.getInstance(document, new FileOutputStream(nomFichier));
            document.open();

            // Polices personnalisées
            Font fontTitre = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
            Font fontNormal = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);
            Font fontGras = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK);

            // En-tête
            Paragraph titre = new Paragraph("PHARMACIE CENTRALE", fontTitre);
            titre.setAlignment(Element.ALIGN_CENTER);
            document.add(titre);
            document.add(new Paragraph("123 Boulevard Mohammed V, Casablanca", fontNormal));
            document.add(new Paragraph("Tél : 05 22 00 00 00\n\n"));

            // Informations de la facture
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            document.add(new Paragraph("FACTURE N° : " + vente.getId(), fontGras));
            document.add(new Paragraph("Date : " + sdf.format(vente.getDateVente()), fontNormal));
            document.add(new Paragraph("Client : " + vente.getNomClient() + "\n\n", fontNormal));

            // Tableau du Total
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            PdfPCell cellLibelle = new PdfPCell(new Phrase("MONTANT TOTAL PAYÉ", fontGras));
            cellLibelle.setBorder(Rectangle.BOTTOM | Rectangle.TOP);
            cellLibelle.setPadding(10);

            PdfPCell cellMontant = new PdfPCell(new Phrase(String.format("%.2f Dhs", vente.getMontantTotal()), fontGras));
            cellMontant.setBorder(Rectangle.BOTTOM | Rectangle.TOP);
            cellMontant.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellMontant.setPadding(10);

            table.addCell(cellLibelle);
            table.addCell(cellMontant);
            document.add(table);

            // Pied de page
            Paragraph footer = new Paragraph("\nMerci de votre visite et prompt rétablissement !", new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}