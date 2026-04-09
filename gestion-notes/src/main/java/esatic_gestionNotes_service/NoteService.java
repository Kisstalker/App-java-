package esatic_gestionNotes_service;


import esatic_gestionNotes_dao.EtudiantDAO;
import esatic_gestionNotes_dao.NoteDAO;
import esatic_gestionNotes_model.Etudiant;
import esatic_gestionNotes_model.Evaluation;
import esatic_gestionNotes_model.Note;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service métier pour la gestion des notes.
 * Orchestre les opérations entre NoteDAO et EtudiantDAO.
 */
public class NoteService {

    private final NoteDAO     noteDAO;
    public NoteService() {
        this.noteDAO     = new NoteDAO();
        new EtudiantDAO();
    }

    
    // SAISIE D'UNE NOTE

    /**
     * Enregistre ou met à jour la note d'un étudiant pour une évaluation.
     * Valide que la valeur est comprise entre 0 et 20.
     *
     * @param idEvaluation  identifiant de l'évaluation
     * @param idEtudiant    identifiant de l'étudiant
     * @param valeur        valeur de la note (null = non saisie)
     * @return true si l'enregistrement a réussi
     * @throws IllegalArgumentException si la note est hors de [0, 20]
     */
    public boolean saisirNote(int idEvaluation, int idEtudiant, Double valeur) {
        if (valeur != null && (valeur < 0 || valeur > 20)) {
            throw new IllegalArgumentException(
                "La note doit être comprise entre 0 et 20. Valeur reçue : " + valeur);
        }

        Note note = new Note();
        note.setIdEvaluation(idEvaluation);
        note.setIdEtudiant(idEtudiant);
        note.setValeur(valeur);
        note.setBonus(0);
        note.setMalus(0);

        // Récupère la note existante pour conserver les bonus/malus déjà appliqués
        Note existante = noteDAO.trouverNote(idEvaluation, idEtudiant);
        if (existante != null) {
            note.setBonus(existante.getBonus());
            note.setMalus(existante.getMalus());
        }

        return noteDAO.enregistrerNote(note);
    }

    
    // BONUS / MALUS

    /**
     * Applique un bonus ou un malus sur la note d'un étudiant.
     * Le résultat final est plafonné entre 0 et 20.
     *
     * @param idEvaluation identifiant de l'évaluation
     * @param idEtudiant   identifiant de l'étudiant
     * @param bonus        points à ajouter (≥ 0)
     * @param malus        points à retirer (≥ 0)
     * @throws IllegalArgumentException si bonus ou malus sont négatifs
     */
    public boolean appliquerBonusMalus(int idEvaluation, int idEtudiant,
                                       double bonus, double malus) {
        if (bonus < 0 || malus < 0) {
            throw new IllegalArgumentException(
                "Le bonus et le malus doivent être des valeurs positives.");
        }

        Note note = noteDAO.trouverNote(idEvaluation, idEtudiant);

        if (note == null) {
            // Crée une entrée vide si la note n'a pas encore été saisie
            note = new Note();
            note.setIdEvaluation(idEvaluation);
            note.setIdEtudiant(idEtudiant);
            note.setValeur(null);
            note.setBonus(bonus);
            note.setMalus(malus);
            return noteDAO.enregistrerNote(note);
        }

        return noteDAO.appliquerBonusMalus(note.getId(), bonus, malus);
    }

   
    // CALCUL DE MOYENNE

    /**
     * Calcule la moyenne d'un étudiant pour un cours.
     *
     * @return moyenne arrondie à 2 décimales, ou -1 si aucune note saisie
     */
    public double calculerMoyenne(int idEtudiant, int idCours) {
        return noteDAO.calculerMoyenne(idEtudiant, idCours);
    }

    /**
     * Calcule les moyennes de TOUS les étudiants d'un cours.
     * Retourne une map : idEtudiant → moyenne
     *
     * @param idCours     identifiant du cours
     * @param etudiants   liste des étudiants du cours
     * @return Map<idEtudiant, moyenne>
     */
    public Map<Integer, Double> calculerToutesMoyennes(int idCours,
                                                        List<Etudiant> etudiants) {
        Map<Integer, Double> moyennes = new HashMap<>();
        for (Etudiant e : etudiants) {
            double moyenne = noteDAO.calculerMoyenne(e.getId(), idCours);
            moyennes.put(e.getId(), moyenne);
        }
        return moyennes;
    }

    
    // RÉCUPÉRATION DES NOTES POUR L'AFFICHAGE

    /**
     * Retourne les évaluations d'un cours.
     */
    public List<Evaluation> getEvaluations(int idCours) {
        return noteDAO.listerEvaluations(idCours);
    }

    /**
     * Retourne la note d'un étudiant pour une évaluation.
     * Crée une note vide si elle n'existe pas encore.
     */
    public Note getOuCreerNote(int idEvaluation, int idEtudiant) {
        Note note = noteDAO.trouverNote(idEvaluation, idEtudiant);
        if (note == null) {
            note = new Note();
            note.setIdEvaluation(idEvaluation);
            note.setIdEtudiant(idEtudiant);
            note.setValeur(null);
            note.setBonus(0);
            note.setMalus(0);
        }
        return note;
    }

    /**
     * Retourne toutes les notes d'un étudiant pour un cours.
     */
    public List<Note> getNotesEtudiant(int idEtudiant, int idCours) {
        return noteDAO.listerNotesEtudiant(idEtudiant, idCours);
    }

   
    // GESTION DES ÉVALUATIONS

    /**
     * Ajoute une nouvelle évaluation à un cours.
     * Vérifie qu'un libellé est bien renseigné.
     */
    public boolean ajouterEvaluation(Evaluation evaluation) {
        if (evaluation.getLibelle() == null || evaluation.getLibelle().trim().isEmpty()) {
            throw new IllegalArgumentException("Le libellé de l'évaluation est obligatoire.");
        }
        if (evaluation.getCoefficient() <= 0) {
            throw new IllegalArgumentException("Le coefficient doit être supérieur à 0.");
        }
        return noteDAO.ajouterEvaluation(evaluation);
    }

    /**
     * Modifie le libellé ou le coefficient d'une évaluation.
     */
    public boolean modifierEvaluation(Evaluation evaluation) {
        if (evaluation.getCoefficient() <= 0) {
            throw new IllegalArgumentException("Le coefficient doit être supérieur à 0.");
        }
        return noteDAO.modifierEvaluation(evaluation);
    }

    /**
     * Supprime une évaluation et toutes ses notes associées.
     */
    public boolean supprimerEvaluation(int idEvaluation) {
        return noteDAO.supprimerEvaluation(idEvaluation);
    }
}