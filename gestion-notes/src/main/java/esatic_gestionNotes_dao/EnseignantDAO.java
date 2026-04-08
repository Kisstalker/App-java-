package esatic_gestionNotes_dao;


import esatic_gestionNotes_model.Enseignant;
import esatic_gestionNotes_util.ConnexionBDD;

import java.sql.*;

/**
 * DAO pour la table {@code enseignant}.
 * Gère l'authentification et la récupération des enseignants.
 */
public class EnseignantDAO {

    private final Connection connexion;

    public EnseignantDAO() {
        this.connexion = ConnexionBDD.getInstance().getConnection();
    }

 
    // AUTHENTIFICATION
    /**
     * Vérifie les identifiants de l'enseignant.
     * Retourne l'enseignant si trouvé, null sinon.
     *
     * @param email      email de connexion
     * @param motDePasse mot de passe en clair (comparé au hash BCrypt)
     * @return {@link Enseignant} ou null
     */
    public Enseignant authentifier(String email, String motDePasse) {
        String sql = "SELECT id, nom, prenoms, email, mot_de_passe "
                   + "FROM enseignant WHERE email = ?";

        try (PreparedStatement stmt = connexion.prepareStatement(sql)) {
            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String hashStocke = rs.getString("mot_de_passe");

                    // Vérification BCrypt
                    boolean valide = at.favre.lib.crypto.bcrypt.BCrypt
                            .verifyer()
                            .verify(motDePasse.toCharArray(), hashStocke)
                            .verified;

                    if (valide) {
                        Enseignant e = new Enseignant();
                        e.setId(rs.getInt("id"));
                        e.setNom(rs.getString("nom"));
                        e.setPrenoms(rs.getString("prenoms"));
                        e.setEmail(rs.getString("email"));
                        return e;
                    }
                }
            }
        } catch (SQLException ex) {
            System.err.println("[EnseignantDAO] Erreur authentification : " + ex.getMessage());
        }
        return null;
    }


    // CRÉER UN ENSEIGNANT
    /**
     * Insère un nouvel enseignant en hashant son mot de passe.
     *
     * @param enseignant objet à persister (mot de passe en clair)
     * @return true si l'insertion a réussi
     */
    public boolean creer(Enseignant enseignant) {
        String sql = "INSERT INTO enseignant (nom, prenoms, email, mot_de_passe) "
                   + "VALUES (?, ?, ?, ?)";

        // Hashage BCrypt du mot de passe avant stockage
        String hash = at.favre.lib.crypto.bcrypt.BCrypt.withDefaults()
                .hashToString(12, enseignant.getMotDePasse().toCharArray());

        try (PreparedStatement stmt = connexion.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, enseignant.getNom());
            stmt.setString(2, enseignant.getPrenoms());
            stmt.setString(3, enseignant.getEmail());
            stmt.setString(4, hash);

            int lignes = stmt.executeUpdate();
            if (lignes > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) enseignant.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException ex) {
            System.err.println("[EnseignantDAO] Erreur création : " + ex.getMessage());
        }
        return false;
    }


    // TROUVER PAR ID

    public Enseignant trouverParId(int id) {
        String sql = "SELECT id, nom, prenoms, email FROM enseignant WHERE id = ?";

        try (PreparedStatement stmt = connexion.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Enseignant(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenoms"),
                        rs.getString("email")
                    );
                }
            }
        } catch (SQLException ex) {
            System.err.println("[EnseignantDAO] Erreur trouverParId : " + ex.getMessage());
        }
        return null;
    }
}