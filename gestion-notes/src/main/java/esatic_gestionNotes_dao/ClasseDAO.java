package esatic_gestionNotes_dao;


import esatic_gestionNotes_model.Classe;
import esatic_gestionNotes_util.ConnexionBDD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table {@code classe}.
 */
public class ClasseDAO {

    private final Connection connexion;

    public ClasseDAO() {
        this.connexion = ConnexionBDD.getInstance().getConnection();
    }


    // LISTER TOUTES LES CLASSES


    public List<Classe> listerToutes() {
        List<Classe> liste = new ArrayList<>();
        String sql = "SELECT id, libelle FROM classe ORDER BY libelle";

        try (Statement stmt = connexion.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {

            while (rs.next()) {
                liste.add(new Classe(rs.getInt("id"), rs.getString("libelle")));
            }
        } catch (SQLException ex) {
            System.err.println("[ClasseDAO] Erreur listerToutes : " + ex.getMessage());
        }
        return liste;
    }

    // CRÉER UNE CLASSE

    public boolean creer(Classe classe) {
        String sql = "INSERT INTO classe (libelle) VALUES (?)";

        try (PreparedStatement stmt = connexion.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, classe.getLibelle());
            int lignes = stmt.executeUpdate();

            if (lignes > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) classe.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException ex) {
            System.err.println("[ClasseDAO] Erreur création : " + ex.getMessage());
        }
        return false;
    }

  
    // TROUVER PAR ID

    public Classe trouverParId(int id) {
        String sql = "SELECT id, libelle FROM classe WHERE id = ?";

        try (PreparedStatement stmt = connexion.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Classe(rs.getInt("id"), rs.getString("libelle"));
                }
            }
        } catch (SQLException ex) {
            System.err.println("[ClasseDAO] Erreur trouverParId : " + ex.getMessage());
        }
        return null;
    }

   
    // SUPPRIMER

    public boolean supprimer(int id) {
        String sql = "DELETE FROM classe WHERE id = ?";

        try (PreparedStatement stmt = connexion.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("[ClasseDAO] Erreur suppression : " + ex.getMessage());
        }
        return false;
    }
}