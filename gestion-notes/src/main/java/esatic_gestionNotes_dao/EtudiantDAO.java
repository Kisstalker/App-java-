package esatic_gestionNotes_dao;

import esatic_gestionNotes_model.Etudiant;
import esatic_gestionNotes_util.ConnexionBDD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table {@code etudiant} et {@code inscription}.
 */
public class EtudiantDAO {

    private final Connection connexion;

    public EtudiantDAO() {
        this.connexion = ConnexionBDD.getInstance().getConnection();
    }


    // LISTER LES ÉTUDIANTS D'UN COURS (via classe + inscription)

    /**
     * Retourne tous les étudiants inscrits dans la classe associée au cours.
     * Triés par nom puis prénom (ordre fiche ESATIC).
     */
    public List<Etudiant> listerParCours(int idCours) {
        List<Etudiant> liste = new ArrayList<>();
        String sql =
            "SELECT e.id, e.matricule, e.nom, e.prenoms, e.statut "
          + "FROM etudiant e "
          + "JOIN inscription ins ON ins.id_etudiant = e.id "
          + "JOIN cours c         ON c.id_classe     = ins.id_classe "
          + "WHERE c.id = ? "
          + "ORDER BY e.nom, e.prenoms";

        try (PreparedStatement stmt = connexion.prepareStatement(sql)) {
            stmt.setInt(1, idCours);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    liste.add(new Etudiant(
                        rs.getInt   ("id"),
                        rs.getString("matricule"),
                        rs.getString("nom"),
                        rs.getString("prenoms"),
                        rs.getString("statut")
                    ));
                }
            }
        } catch (SQLException ex) {
            System.err.println("[EtudiantDAO] Erreur listerParCours : " + ex.getMessage());
        }
        return liste;
    }

   
    // CRÉER UN ÉTUDIANT + L'INSCRIRE DANS UNE CLASSE

    /**
     * Insère un étudiant et l'inscrit immédiatement dans une classe.
     *
     * @param etudiant  l'étudiant à créer
     * @param idClasse  la classe dans laquelle l'inscrire
     * @param annee     l'année scolaire (ex : 2024)
     */
    public boolean creer(Etudiant etudiant, int idClasse, int annee) {
        String sqlEtudiant = "INSERT INTO etudiant (matricule, nom, prenoms, statut) "
                           + "VALUES (?, ?, ?, ?)";
        String sqlInscription = "INSERT INTO inscription (id_etudiant, id_classe, annee) "
                              + "VALUES (?, ?, ?)";

        try {
            connexion.setAutoCommit(false); // Transaction

            // 1. Insérer l'étudiant
            try (PreparedStatement stmt = connexion.prepareStatement(sqlEtudiant,
                    Statement.RETURN_GENERATED_KEYS)) {

                stmt.setString(1, etudiant.getMatricule());
                stmt.setString(2, etudiant.getNom());
                stmt.setString(3, etudiant.getPrenoms());
                stmt.setString(4, etudiant.getStatut() != null
                                  ? etudiant.getStatut() : "N");
                stmt.executeUpdate();

                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) etudiant.setId(keys.getInt(1));
                }
            }

            // 2. Inscrire dans la classe
            try (PreparedStatement stmt = connexion.prepareStatement(sqlInscription)) {
                stmt.setInt(1, etudiant.getId());
                stmt.setInt(2, idClasse);
                stmt.setInt(3, annee);
                stmt.executeUpdate();
            }

            connexion.commit();
            return true;

        } catch (SQLException ex) {
            try { connexion.rollback(); } catch (SQLException ignored) {}
            System.err.println("[EtudiantDAO] Erreur création : " + ex.getMessage());
        } finally {
            try { connexion.setAutoCommit(true); } catch (SQLException ignored) {}
        }
        return false;
    }


    // MODIFIER UN ÉTUDIANT

    public boolean modifier(Etudiant etudiant) {
        String sql = "UPDATE etudiant SET matricule = ?, nom = ?, "
                   + "prenoms = ?, statut = ? WHERE id = ?";

        try (PreparedStatement stmt = connexion.prepareStatement(sql)) {
            stmt.setString(1, etudiant.getMatricule());
            stmt.setString(2, etudiant.getNom());
            stmt.setString(3, etudiant.getPrenoms());
            stmt.setString(4, etudiant.getStatut());
            stmt.setInt   (5, etudiant.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("[EtudiantDAO] Erreur modification : " + ex.getMessage());
        }
        return false;
    }


    // SUPPRIMER UN ÉTUDIANT

    public boolean supprimer(int id) {
        String sql = "DELETE FROM etudiant WHERE id = ?";

        try (PreparedStatement stmt = connexion.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("[EtudiantDAO] Erreur suppression : " + ex.getMessage());
        }
        return false;
    }

    // VÉRIFIER SI UN MATRICULE EXISTE DÉJÀ

    public boolean matriculeExiste(String matricule) {
        String sql = "SELECT COUNT(*) FROM etudiant WHERE matricule = ?";

        try (PreparedStatement stmt = connexion.prepareStatement(sql)) {
            stmt.setString(1, matricule);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            System.err.println("[EtudiantDAO] Erreur matriculeExiste : " + ex.getMessage());
        }
        return false;
    }
}