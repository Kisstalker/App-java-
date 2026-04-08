package esatic_gestionNotes_dao;

import esatic_gestionNotes_model.Evaluation;
import esatic_gestionNotes_model.Note;
import esatic_gestionNotes_util.ConnexionBDD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour les tables {@code note} et {@code evaluation}.
 * Gère la saisie, modification, bonus/malus et calcul de moyenne.
 */
public class NoteDAO {

    private final Connection connexion;

    public NoteDAO() {
        this.connexion = ConnexionBDD.getInstance().getConnection();
    }

   
    // ÉVALUATIONS

    /**
     * Retourne toutes les évaluations d'un cours, triées par id (ordre de création).
     */
    public List<Evaluation> listerEvaluations(int idCours) {
        List<Evaluation> liste = new ArrayList<>();
        String sql = "SELECT id, libelle, coefficient, id_cours "
                   + "FROM evaluation WHERE id_cours = ? ORDER BY id";

        try (PreparedStatement stmt = connexion.prepareStatement(sql)) {
            stmt.setInt(1, idCours);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    liste.add(new Evaluation(
                        rs.getInt   ("id"),
                        rs.getString("libelle"),
                        rs.getDouble("coefficient"),
                        rs.getInt   ("id_cours")
                    ));
                }
            }
        } catch (SQLException ex) {
            System.err.println("[NoteDAO] Erreur listerEvaluations : " + ex.getMessage());
        }
        return liste;
    }

    /**
     * Ajoute une nouvelle évaluation à un cours existant.
     */
    public boolean ajouterEvaluation(Evaluation evaluation) {
        String sql = "INSERT INTO evaluation (libelle, coefficient, id_cours) "
                   + "VALUES (?, ?, ?)";

        try (PreparedStatement stmt = connexion.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, evaluation.getLibelle());
            stmt.setDouble(2, evaluation.getCoefficient());
            stmt.setInt   (3, evaluation.getIdCours());

            int lignes = stmt.executeUpdate();
            if (lignes > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) evaluation.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException ex) {
            System.err.println("[NoteDAO] Erreur ajouterEvaluation : " + ex.getMessage());
        }
        return false;
    }

    /**
     * Modifie le libellé et le coefficient d'une évaluation.
     */
    public boolean modifierEvaluation(Evaluation evaluation) {
        String sql = "UPDATE evaluation SET libelle = ?, coefficient = ? WHERE id = ?";

        try (PreparedStatement stmt = connexion.prepareStatement(sql)) {
            stmt.setString(1, evaluation.getLibelle());
            stmt.setDouble(2, evaluation.getCoefficient());
            stmt.setInt   (3, evaluation.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("[NoteDAO] Erreur modifierEvaluation : " + ex.getMessage());
        }
        return false;
    }

    /**
     * Supprime une évaluation (et toutes ses notes en cascade).
     */
    public boolean supprimerEvaluation(int idEvaluation) {
        String sql = "DELETE FROM evaluation WHERE id = ?";

        try (PreparedStatement stmt = connexion.prepareStatement(sql)) {
            stmt.setInt(1, idEvaluation);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("[NoteDAO] Erreur supprimerEvaluation : " + ex.getMessage());
        }
        return false;
    }


    // NOTES

    /**
     * Retourne la note d'un étudiant pour une évaluation.
     * Retourne null si aucune ligne trouvée.
     */
    public Note trouverNote(int idEvaluation, int idEtudiant) {
        String sql = "SELECT id, valeur, bonus, malus, id_evaluation, id_etudiant "
                   + "FROM note WHERE id_evaluation = ? AND id_etudiant = ?";

        try (PreparedStatement stmt = connexion.prepareStatement(sql)) {
            stmt.setInt(1, idEvaluation);
            stmt.setInt(2, idEtudiant);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Double valeur = rs.getObject("valeur") != null
                                    ? rs.getDouble("valeur") : null;
                    return new Note(
                        rs.getInt   ("id"),
                        valeur,
                        rs.getDouble("bonus"),
                        rs.getDouble("malus"),
                        rs.getInt   ("id_evaluation"),
                        rs.getInt   ("id_etudiant")
                    );
                }
            }
        } catch (SQLException ex) {
            System.err.println("[NoteDAO] Erreur trouverNote : " + ex.getMessage());
        }
        return null;
    }

    /**
     * Enregistre ou met à jour une note (INSERT ... ON DUPLICATE KEY UPDATE).
     * Fonctionne que la note existe déjà ou non.
     */
    public boolean enregistrerNote(Note note) {
        String sql =
            "INSERT INTO note (valeur, bonus, malus, id_evaluation, id_etudiant) "
          + "VALUES (?, ?, ?, ?, ?) "
          + "ON DUPLICATE KEY UPDATE valeur = VALUES(valeur), "
          + "bonus = VALUES(bonus), malus = VALUES(malus)";

        try (PreparedStatement stmt = connexion.prepareStatement(sql)) {
            if (note.getValeur() != null) {
                stmt.setDouble(1, note.getValeur());
            } else {
                stmt.setNull(1, Types.DECIMAL);
            }
            stmt.setDouble(2, note.getBonus());
            stmt.setDouble(3, note.getMalus());
            stmt.setInt   (4, note.getIdEvaluation());
            stmt.setInt   (5, note.getIdEtudiant());

            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("[NoteDAO] Erreur enregistrerNote : " + ex.getMessage());
        }
        return false;
    }

    /**
     * Applique un bonus ou malus à une note existante.
     */
    public boolean appliquerBonusMalus(int idNote, double bonus, double malus) {
        String sql = "UPDATE note SET bonus = ?, malus = ? WHERE id = ?";

        try (PreparedStatement stmt = connexion.prepareStatement(sql)) {
            stmt.setDouble(1, bonus);
            stmt.setDouble(2, malus);
            stmt.setInt   (3, idNote);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("[NoteDAO] Erreur appliquerBonusMalus : " + ex.getMessage());
        }
        return false;
    }

    // CALCUL DE MOYENNE


    /**
     * Calcule la moyenne pondérée d'un étudiant pour un cours.
     * Ne tient compte que des notes saisies (non nulles).
     *
     * @return la moyenne arrondie à 2 décimales, ou -1 si aucune note saisie
     */
    public double calculerMoyenne(int idEtudiant, int idCours) {
        String sql =
            "SELECT ROUND( "
          + "  SUM(CASE WHEN n.valeur IS NOT NULL "
          + "      THEN (n.valeur + n.bonus - n.malus) * ev.coefficient END) / "
          + "  NULLIF(SUM(CASE WHEN n.valeur IS NOT NULL "
          + "      THEN ev.coefficient END), 0) "
          + ", 2) AS moyenne "
          + "FROM note n "
          + "JOIN evaluation ev ON ev.id = n.id_evaluation "
          + "WHERE n.id_etudiant = ? AND ev.id_cours = ?";

        try (PreparedStatement stmt = connexion.prepareStatement(sql)) {
            stmt.setInt(1, idEtudiant);
            stmt.setInt(2, idCours);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getObject("moyenne") != null) {
                    return rs.getDouble("moyenne");
                }
            }
        } catch (SQLException ex) {
            System.err.println("[NoteDAO] Erreur calculerMoyenne : " + ex.getMessage());
        }
        return -1;
    }

    /**
     * Retourne toutes les notes d'un étudiant pour un cours donné.
     */
    public List<Note> listerNotesEtudiant(int idEtudiant, int idCours) {
        List<Note> liste = new ArrayList<>();
        String sql =
            "SELECT n.id, n.valeur, n.bonus, n.malus, n.id_evaluation, n.id_etudiant "
          + "FROM note n "
          + "JOIN evaluation ev ON ev.id = n.id_evaluation "
          + "WHERE n.id_etudiant = ? AND ev.id_cours = ? "
          + "ORDER BY ev.id";

        try (PreparedStatement stmt = connexion.prepareStatement(sql)) {
            stmt.setInt(1, idEtudiant);
            stmt.setInt(2, idCours);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Double valeur = rs.getObject("valeur") != null
                                    ? rs.getDouble("valeur") : null;
                    liste.add(new Note(
                        rs.getInt   ("id"),
                        valeur,
                        rs.getDouble("bonus"),
                        rs.getDouble("malus"),
                        rs.getInt   ("id_evaluation"),
                        rs.getInt   ("id_etudiant")
                    ));
                }
            }
        } catch (SQLException ex) {
            System.err.println("[NoteDAO] Erreur listerNotesEtudiant : " + ex.getMessage());
        }
        return liste;
    }
}