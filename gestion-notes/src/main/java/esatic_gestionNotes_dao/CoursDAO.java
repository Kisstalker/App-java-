package esatic_gestionNotes_dao;


import esatic_gestionNotes_model.Classe;
import esatic_gestionNotes_model.Cours;
import esatic_gestionNotes_model.Enseignant;
import esatic_gestionNotes_util.ConnexionBDD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table {@code cours}.
 */
public class CoursDAO {

    private final Connection connexion;

    public CoursDAO() {
        this.connexion = ConnexionBDD.getInstance().getConnection();
    }

   
    // CRÉER UN COURS

    /**
     * Crée un cours ET génère automatiquement 2 évaluations par défaut.
     */
    public boolean creer(Cours cours) {
        String sqlCours = "INSERT INTO cours (nom, volume_horaire, formule_calcul, "
                        + "id_enseignant, id_classe) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connexion.prepareStatement(sqlCours,
                Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, cours.getNom());
            stmt.setInt   (2, cours.getVolumeHoraire());
            stmt.setString(3, cours.getFormuleCalcul() != null
                              ? cours.getFormuleCalcul() : "MOYENNE_PONDEREE");
            stmt.setInt   (4, cours.getEnseignant().getId());
            stmt.setInt   (5, cours.getClasse().getId());

            int lignes = stmt.executeUpdate();
            if (lignes > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        int idCours = keys.getInt(1);
                        cours.setId(idCours);
                        // Création des 2 évaluations par défaut
                        creerEvaluationsParDefaut(idCours);
                    }
                }
                return true;
            }
        } catch (SQLException ex) {
            System.err.println("[CoursDAO] Erreur création : " + ex.getMessage());
        }
        return false;
    }

    /**
     * Insère 2 évaluations (Note 1 et Note 2) pour un cours nouvellement créé.
     */
    private void creerEvaluationsParDefaut(int idCours) throws SQLException {
        String sql = "INSERT INTO evaluation (libelle, coefficient, id_cours) VALUES (?, 1.00, ?)";

        try (PreparedStatement stmt = connexion.prepareStatement(sql)) {
            stmt.setString(1, "Note 1");
            stmt.setInt   (2, idCours);
            stmt.executeUpdate();

            stmt.setString(1, "Note 2");
            stmt.setInt   (2, idCours);
            stmt.executeUpdate();
        }
    }


    // LISTER LES COURS D'UN ENSEIGNANT

    public List<Cours> listerParEnseignant(int idEnseignant) {
        List<Cours> liste = new ArrayList<>();
        String sql =
            "SELECT c.id, c.nom, c.volume_horaire, c.formule_calcul, "
          + "       cl.id AS cl_id, cl.libelle "
          + "FROM cours c "
          + "JOIN classe cl ON cl.id = c.id_classe "
          + "WHERE c.id_enseignant = ? "
          + "ORDER BY c.nom";

        try (PreparedStatement stmt = connexion.prepareStatement(sql)) {
            stmt.setInt(1, idEnseignant);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Classe classe = new Classe(rs.getInt("cl_id"), rs.getString("libelle"));
                    Enseignant e  = new Enseignant();
                    e.setId(idEnseignant);

                    Cours cours = new Cours(
                        rs.getInt   ("id"),
                        rs.getString("nom"),
                        rs.getInt   ("volume_horaire"),
                        rs.getString("formule_calcul"),
                        e, classe
                    );
                    liste.add(cours);
                }
            }
        } catch (SQLException ex) {
            System.err.println("[CoursDAO] Erreur listerParEnseignant : " + ex.getMessage());
        }
        return liste;
    }


    // TROUVER PAR ID

    public Cours trouverParId(int id) {
        String sql =
            "SELECT c.id, c.nom, c.volume_horaire, c.formule_calcul, "
          + "       cl.id AS cl_id, cl.libelle, "
          + "       e.id AS e_id, e.nom AS e_nom, e.prenoms AS e_prenoms "
          + "FROM cours c "
          + "JOIN classe cl    ON cl.id = c.id_classe "
          + "JOIN enseignant e ON e.id  = c.id_enseignant "
          + "WHERE c.id = ?";

        try (PreparedStatement stmt = connexion.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Classe     cl = new Classe(rs.getInt("cl_id"), rs.getString("libelle"));
                    Enseignant e  = new Enseignant(
                        rs.getInt("e_id"), rs.getString("e_nom"),
                        rs.getString("e_prenoms"), null
                    );
                    return new Cours(
                        rs.getInt   ("id"),
                        rs.getString("nom"),
                        rs.getInt   ("volume_horaire"),
                        rs.getString("formule_calcul"),
                        e, cl
                    );
                }
            }
        } catch (SQLException ex) {
            System.err.println("[CoursDAO] Erreur trouverParId : " + ex.getMessage());
        }
        return null;
    }

  
    // MODIFIER

    public boolean modifier(Cours cours) {
        String sql = "UPDATE cours SET nom = ?, volume_horaire = ?, "
                   + "formule_calcul = ? WHERE id = ?";

        try (PreparedStatement stmt = connexion.prepareStatement(sql)) {
            stmt.setString(1, cours.getNom());
            stmt.setInt   (2, cours.getVolumeHoraire());
            stmt.setString(3, cours.getFormuleCalcul());
            stmt.setInt   (4, cours.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("[CoursDAO] Erreur modification : " + ex.getMessage());
        }
        return false;
    }

    
    // SUPPRIMER

    public boolean supprimer(int id) {
        String sql = "DELETE FROM cours WHERE id = ?";

        try (PreparedStatement stmt = connexion.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("[CoursDAO] Erreur suppression : " + ex.getMessage());
        }
        return false;
    }
}