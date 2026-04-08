package esatic_gestionNotes_service;

import esatic_gestionNotes_model.Cours;
import esatic_gestionNotes_model.Etudiant;
import esatic_gestionNotes_model.Evaluation;
import esatic_gestionNotes_model.Note;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Service de génération de la Fiche de Notes Contrôle Continu.
 * Produit un fichier PDF fidèle au modèle ESATIC.
 */
public class ExportPdfService {

    // ── Couleurs ESATIC ──────────────────────────────────────────────────────
    private static final BaseColor COULEUR_ENTETE   = new BaseColor(52, 73, 94);   // bleu foncé
    private static final BaseColor COULEUR_SOUS_TITRE = new BaseColor(236, 240, 241); // gris clair
    private static final BaseColor COULEUR_COLONNE_MOY = new BaseColor(189, 195, 199); // colonne grisée

    // ── Polices ──────────────────────────────────────────────────────────────
    private Font fontTitre;
    private Font fontSousTitre;
    private Font fontEnteteCol;
    private Font fontDonnees;
    private Font fontMoyenne;

    public ExportPdfService() {
        initialiserPolices();
    }

    private void initialiserPolices() {
        fontTitre     = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD,   BaseColor.WHITE);
        fontSousTitre = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD,   BaseColor.DARK_GRAY);
        fontEnteteCol = new Font(Font.FontFamily.HELVETICA,  8, Font.BOLD,   BaseColor.WHITE);
        fontDonnees   = new Font(Font.FontFamily.HELVETICA,  8, Font.NORMAL, BaseColor.BLACK);
        fontMoyenne   = new Font(Font.FontFamily.HELVETICA,  8, Font.BOLD,   BaseColor.DARK_GRAY);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POINT D'ENTRÉE PRINCIPAL
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Génère la fiche de notes PDF et la sauvegarde dans {@code fichierSortie}.
     *
     * @param cours         le cours concerné
     * @param evaluations   liste des évaluations du cours
     * @param etudiants     liste des étudiants
     * @param notes         map : idEtudiant → liste de ses notes
     * @param moyennes      map : idEtudiant → moyenne CC
     * @param fichierSortie fichier PDF de destination
     * @throws DocumentException en cas d'erreur iText
     * @throws IOException       en cas d'erreur d'écriture
     */
    public void genererFiche(Cours cours,
                             List<Evaluation> evaluations,
                             List<Etudiant> etudiants,
                             Map<Integer, List<Note>> notes,
                             Map<Integer, Double> moyennes,
                             File fichierSortie)
            throws DocumentException, IOException {

        Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);

        try (FileOutputStream fos = new FileOutputStream(fichierSortie)) {
            PdfWriter.getInstance(document, fos);
            document.open();

            // 1. En-tête ESATIC
            document.add(construireEnTete(cours));
            document.add(Chunk.NEWLINE);

            // 2. Sous-titre (nom de la classe)
            document.add(construireSousTitre(cours));
            document.add(Chunk.NEWLINE);

            // 3. Tableau des notes
            document.add(construireTableau(evaluations, etudiants, notes, moyennes));

            // 4. Pied de page (signature enseignant)
            document.add(Chunk.NEWLINE);
            document.add(construirePiedDePage(cours));
        }

        document.close();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // EN-TÊTE
    // ─────────────────────────────────────────────────────────────────────────

    private PdfPTable construireEnTete(Cours cours) throws DocumentException {
        PdfPTable entete = new PdfPTable(2);
        entete.setWidthPercentage(100);
        entete.setWidths(new float[]{2f, 5f});

        // Cellule logo / nom école
        PdfPCell cellEcole = new PdfPCell();
        cellEcole.setBackgroundColor(COULEUR_ENTETE);
        cellEcole.setPadding(10);
        cellEcole.setBorder(Rectangle.NO_BORDER);

        Paragraph nomEcole = new Paragraph();
        nomEcole.add(new Chunk("ESATIC\n", new Font(Font.FontFamily.HELVETICA,
                16, Font.BOLD, BaseColor.WHITE)));
        nomEcole.add(new Chunk("École Supérieure Africaine des TIC",
                new Font(Font.FontFamily.HELVETICA, 7, Font.ITALIC, BaseColor.WHITE)));
        cellEcole.addElement(nomEcole);
        entete.addCell(cellEcole);

        // Cellule titre principal
        PdfPCell cellTitre = new PdfPCell();
        cellTitre.setBackgroundColor(COULEUR_SOUS_TITRE);
        cellTitre.setPadding(12);
        cellTitre.setBorder(Rectangle.NO_BORDER);
        cellTitre.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellTitre.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph titre = new Paragraph(
            "FICHE DE NOTES CONTRÔLE CONTINU",
            new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, COULEUR_ENTETE)
        );
        titre.setAlignment(Element.ALIGN_CENTER);
        cellTitre.addElement(titre);

        Paragraph matiere = new Paragraph(
            "Matière : " + cours.getNom(),
            new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.DARK_GRAY)
        );
        matiere.setAlignment(Element.ALIGN_CENTER);
        cellTitre.addElement(matiere);

        entete.addCell(cellTitre);
        return entete;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SOUS-TITRE (NOM DE CLASSE)
    // ─────────────────────────────────────────────────────────────────────────

    private Paragraph construireSousTitre(Cours cours) {
        String classe = cours.getClasse() != null ? cours.getClasse().getLibelle() : "";
        Paragraph p = new Paragraph(classe, fontSousTitre);
        p.setAlignment(Element.ALIGN_CENTER);
        return p;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TABLEAU PRINCIPAL
    // ─────────────────────────────────────────────────────────────────────────

    private PdfPTable construireTableau(List<Evaluation> evaluations,
                                        List<Etudiant>   etudiants,
                                        Map<Integer, List<Note>> notes,
                                        Map<Integer, Double>     moyennes)
            throws DocumentException {

        // Colonnes : N° | Matricule | Statut | Nom | Prénoms | note1 | note2 | ... | MOY.CC
        int nbColonnes = 5 + evaluations.size() + 1;
        PdfPTable tableau = new PdfPTable(nbColonnes);
        tableau.setWidthPercentage(100);

        // Largeurs relatives des colonnes
        float[] largeurs = new float[nbColonnes];
        largeurs[0] = 0.8f;  // N°
        largeurs[1] = 2.5f;  // Matricule
        largeurs[2] = 1.0f;  // Statut
        largeurs[3] = 2.5f;  // Nom
        largeurs[4] = 3.5f;  // Prénoms
        for (int i = 0; i < evaluations.size(); i++) {
            largeurs[5 + i] = 1.5f; // Chaque note
        }
        largeurs[nbColonnes - 1] = 1.8f; // MOY.CC
        tableau.setWidths(largeurs);

        // ── En-têtes des colonnes ─────────────────────────────────────────
        ajouterCelluleEnTete(tableau, "N°");
        ajouterCelluleEnTete(tableau, "MATRICULE");
        ajouterCelluleEnTete(tableau, "STATUT");
        ajouterCelluleEnTete(tableau, "NOM");
        ajouterCelluleEnTete(tableau, "PRÉNOMS");
        for (Evaluation ev : evaluations) {
            ajouterCelluleEnTete(tableau, ev.getLibelle()
                + "\n(coeff " + ev.getCoefficient() + ")");
        }
        ajouterCelluleEnTeteGrise(tableau, "MOY. CC");

        // ── Lignes des étudiants ──────────────────────────────────────────
        for (int idx = 0; idx < etudiants.size(); idx++) {
            Etudiant etudiant = etudiants.get(idx);
            boolean ligneImpaire = idx % 2 == 0;
            BaseColor couleurLigne = ligneImpaire
                ? BaseColor.WHITE
                : new BaseColor(245, 245, 245);

            // N°
            ajouterCelluleDonnee(tableau, String.valueOf(idx + 1), couleurLigne,
                Element.ALIGN_CENTER);
            // Matricule
            ajouterCelluleDonnee(tableau, etudiant.getMatricule(), couleurLigne,
                Element.ALIGN_CENTER);
            // Statut
            ajouterCelluleDonnee(tableau, etudiant.getStatut(), couleurLigne,
                Element.ALIGN_CENTER);
            // Nom
            ajouterCelluleDonnee(tableau, etudiant.getNom().toUpperCase(), couleurLigne,
                Element.ALIGN_LEFT);
            // Prénoms
            ajouterCelluleDonnee(tableau, etudiant.getPrenoms(), couleurLigne,
                Element.ALIGN_LEFT);

            // Notes
            List<Note> notesEtudiant = notes.get(etudiant.getId());
            for (int j = 0; j < evaluations.size(); j++) {
                String valeurAffichee = "—";
                if (notesEtudiant != null && j < notesEtudiant.size()) {
                    Note note = notesEtudiant.get(j);
                    if (note.estSaisie()) {
                        valeurAffichee = String.format("%.2f", note.getValeurEffective());
                    }
                }
                ajouterCelluleDonnee(tableau, valeurAffichee, couleurLigne,
                    Element.ALIGN_CENTER);
            }

            // Moyenne CC (colonne grisée, non modifiable)
            Double moy = moyennes.get(etudiant.getId());
            String moyAffichee = (moy != null && moy >= 0)
                ? String.format("%.2f", moy) : "—";
            ajouterCelluleMoyenne(tableau, moyAffichee);
        }

        return tableau;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PIED DE PAGE
    // ─────────────────────────────────────────────────────────────────────────

    private PdfPTable construirePiedDePage(Cours cours) throws DocumentException {
        PdfPTable pied = new PdfPTable(2);
        pied.setWidthPercentage(100);
        pied.setWidths(new float[]{3f, 2f});

        String nomEnseignant = cours.getEnseignant() != null
            ? cours.getEnseignant().toString() : "_______________";

        PdfPCell cellInfo = new PdfPCell(new Phrase(
            "Enseignant : " + nomEnseignant, fontDonnees));
        cellInfo.setBorder(Rectangle.NO_BORDER);
        pied.addCell(cellInfo);

        PdfPCell cellSignature = new PdfPCell(new Phrase(
            "Signature : _______________________", fontDonnees));
        cellSignature.setBorder(Rectangle.NO_BORDER);
        cellSignature.setHorizontalAlignment(Element.ALIGN_RIGHT);
        pied.addCell(cellSignature);

        return pied;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UTILITAIRES CELLULES
    // ─────────────────────────────────────────────────────────────────────────

    private void ajouterCelluleEnTete(PdfPTable tableau, String texte) {
        PdfPCell cell = new PdfPCell(new Phrase(texte, fontEnteteCol));
        cell.setBackgroundColor(COULEUR_ENTETE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        tableau.addCell(cell);
    }

    private void ajouterCelluleEnTeteGrise(PdfPTable tableau, String texte) {
        PdfPCell cell = new PdfPCell(new Phrase(texte, fontEnteteCol));
        cell.setBackgroundColor(COULEUR_COLONNE_MOY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        tableau.addCell(cell);
    }

    private void ajouterCelluleDonnee(PdfPTable tableau, String texte,
                                       BaseColor fond, int alignement) {
        PdfPCell cell = new PdfPCell(new Phrase(texte != null ? texte : "", fontDonnees));
        cell.setBackgroundColor(fond);
        cell.setHorizontalAlignment(alignement);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4);
        tableau.addCell(cell);
    }

    private void ajouterCelluleMoyenne(PdfPTable tableau, String texte) {
        PdfPCell cell = new PdfPCell(new Phrase(texte, fontMoyenne));
        cell.setBackgroundColor(COULEUR_COLONNE_MOY); // grisée = non modifiable
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4);
        tableau.addCell(cell);
    }
}