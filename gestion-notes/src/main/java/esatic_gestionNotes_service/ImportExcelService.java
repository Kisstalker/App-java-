package esatic_gestionNotes_service;


import esatic_gestionNotes_model.Etudiant;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service d'import d'étudiants depuis un fichier Excel (.xlsx).
 *
 * <p>Format attendu du fichier Excel (ligne 1 = en-têtes ignorées) :</p>
 * <pre>
 *  Colonne A : Matricule
 *  Colonne B : Nom
 *  Colonne C : Prénoms
 *  Colonne D : Statut (N ou R — optionnel, "N" par défaut)
 * </pre>
 */
public class ImportExcelService {

    private final CoursService coursService;

    public ImportExcelService(CoursService coursService) {
        this.coursService = coursService;
    }

    
    // IMPORT DEPUIS FICHIER EXCEL
    /**
     * Résultat d'un import : liste des étudiants importés et des erreurs.
     */
    public static class ResultatImport {
        public final List<Etudiant> importes  = new ArrayList<>();
        public final List<String>   erreurs   = new ArrayList<>();

        public int getNbImportes() { return importes.size(); }
        public int getNbErreurs()  { return erreurs.size(); }
        public boolean aDesErreurs() { return !erreurs.isEmpty(); }
    }

    /**
     * Importe les étudiants depuis un fichier Excel et les inscrit dans une classe.
     *
     * @param fichier   fichier .xlsx à lire
     * @param idClasse  classe dans laquelle inscrire les étudiants importés
     * @return {@link ResultatImport} avec les étudiants créés et les erreurs ligne par ligne
     */
    public ResultatImport importerDepuisExcel(File fichier, int idClasse) {
        ResultatImport resultat = new ResultatImport();

        if (fichier == null || !fichier.exists()) {
            resultat.erreurs.add("Fichier introuvable : " + fichier);
            return resultat;
        }

        try (FileInputStream fis      = new FileInputStream(fichier);
             Workbook         workbook = new XSSFWorkbook(fis)) {

            Sheet feuille = workbook.getSheetAt(0); // Première feuille
            int numeroPremiereLigneData = 1;         // On ignore la ligne 0 (en-têtes)

            for (int i = numeroPremiereLigneData; i <= feuille.getLastRowNum(); i++) {
                Row ligne = feuille.getRow(i);

                if (ligne == null || estLigneVide(ligne)) continue;

                try {
                    Etudiant etudiant = lireLigne(ligne, i + 1);

                    boolean ok = coursService.ajouterEtudiant(etudiant, idClasse);
                    if (ok) {
                        resultat.importes.add(etudiant);
                    } else {
                        resultat.erreurs.add("Ligne " + (i + 1) +
                            " : Échec d'enregistrement pour " + etudiant.getMatricule());
                    }
                } catch (IllegalArgumentException ex) {
                    resultat.erreurs.add("Ligne " + (i + 1) + " : " + ex.getMessage());
                }
            }

        } catch (IOException ex) {
            resultat.erreurs.add("Erreur lecture fichier : " + ex.getMessage());
        }

        return resultat;
    }

    
    // MÉTHODES PRIVÉES

    /**
     * Lit une ligne Excel et construit un objet {@link Etudiant}.
     *
     * @param ligne      ligne Apache POI
     * @param numeroLigne numéro de ligne pour les messages d'erreur
     */
    private Etudiant lireLigne(Row ligne, int numeroLigne) {
        String matricule = lireCellule(ligne, 0);
        String nom       = lireCellule(ligne, 1);
        String prenoms   = lireCellule(ligne, 2);
        String statut    = lireCellule(ligne, 3);

        if (matricule.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Matricule vide à la ligne " + numeroLigne);
        }
        if (nom.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Nom vide à la ligne " + numeroLigne);
        }
        if (prenoms.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Prénoms vides à la ligne " + numeroLigne);
        }

        // Statut par défaut : N (Normal)
        if (statut.trim().isEmpty() || (!statut.equalsIgnoreCase("N")
                              && !statut.equalsIgnoreCase("R"))) {
            statut = "N";
        } else {
            statut = statut.toUpperCase();
        }

        Etudiant e = new Etudiant();
        e.setMatricule(matricule.trim());
        e.setNom(nom.trim().toUpperCase());
        e.setPrenoms(capitaliser(prenoms.trim()));
        e.setStatut(statut);
        return e;
    }

    /**
     * Lit la valeur d'une cellule en tant que String, quel que soit son type.
     */
    private String lireCellule(Row ligne, int colonne) {
        Cell cellule = ligne.getCell(colonne);
        if (cellule == null) return "";

        switch (cellule.getCellType()) {
            case STRING:  return cellule.getStringCellValue().trim();
            case NUMERIC: return String.valueOf((long) cellule.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cellule.getBooleanCellValue());
            default:      return "";
        }
    }

    /**
     * Vérifie si toutes les cellules d'une ligne sont vides.
     */
    private boolean estLigneVide(Row ligne) {
        for (int c = 0; c <= 3; c++) {
            Cell cell = ligne.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK
                    && !lireCellule(ligne, c).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Met en majuscule la première lettre de chaque mot.
     */
    private String capitaliser(String texte) {
        if (texte == null || texte.trim().isEmpty()) return texte;
        String[] mots = texte.toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String mot : mots) {
            if (!mot.isEmpty()) {
                sb.append(Character.toUpperCase(mot.charAt(0)))
                  .append(mot.substring(1))
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }
}