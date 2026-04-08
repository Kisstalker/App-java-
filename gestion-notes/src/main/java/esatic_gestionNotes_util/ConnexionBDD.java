package esatic_gestionNotes_util;


import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * ConnexionBDD — Singleton de gestion de la connexion MySQL.
 *
 * <p>Les paramètres de connexion sont lus depuis le fichier
 * {@code /src/main/resources/config.properties} afin de ne jamais
 * écrire d'identifiants en dur dans le code source.</p>
 *
 * <p>Usage :</p>
 * <pre>{@code
 *   Connection conn = ConnexionBDD.getInstance().getConnection();
 * }</pre>
 *
 * @author  SidickAlaba — SIGL 2
 * @version 1.0
 */
public class ConnexionBDD {

    // ── Instance unique (Singleton) ──────────────────────────────────────────
    private static ConnexionBDD instance;

    // ── Connexion JDBC ───────────────────────────────────────────────────────
    private Connection connection;

    // ── Paramètres chargés depuis config.properties ──────────────────────────
    private String url;
    private String utilisateur;
    private String motDePasse;

    // ─────────────────────────────────────────────────────────────────────────
    // Constructeur privé : charge la config et ouvre la connexion
    // ─────────────────────────────────────────────────────────────────────────
    private ConnexionBDD() {
        chargerConfiguration();
        ouvrirConnexion();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getInstance() : point d'accès unique
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retourne l'instance unique de {@code ConnexionBDD}.
     * Si la connexion est fermée ou nulle, elle est automatiquement
     * rétablie (reconnexion transparente).
     *
     * @return l'instance singleton
     */
    public static synchronized ConnexionBDD getInstance() {
        if (instance == null) {
            instance = new ConnexionBDD();
        }
        return instance;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getConnection() : retourne la connexion, la rétablit si nécessaire
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retourne la connexion JDBC active.
     * Tente une reconnexion automatique si la connexion est perdue.
     *
     * @return {@link Connection} JDBC valide
     * @throws RuntimeException si la reconnexion échoue
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("[ConnexionBDD] Connexion fermée — reconnexion...");
                ouvrirConnexion();
            }
        } catch (SQLException e) {
            System.err.println("[ConnexionBDD] Erreur vérification état connexion : " + e.getMessage());
            ouvrirConnexion();
        }
        return connection;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // fermer() : ferme proprement la connexion
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Ferme la connexion JDBC proprement.
     * À appeler lors de l'arrêt de l'application.
     */
    public void fermer() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("[ConnexionBDD] Connexion fermée avec succès.");
            } catch (SQLException e) {
                System.err.println("[ConnexionBDD] Erreur fermeture connexion : " + e.getMessage());
            } finally {
                connection = null;
                instance   = null;
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Méthodes privées
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Charge les paramètres depuis {@code config.properties}.
     */
    private void chargerConfiguration() {
        Properties props = new Properties();

        try (InputStream is = getClass()
                .getResourceAsStream("/config.properties")) {

            if (is == null) {
                throw new RuntimeException(
                    "[ConnexionBDD] Fichier config.properties introuvable dans le classpath !");
            }

            props.load(is);

            String hote     = props.getProperty("db.host",     "localhost");
            String port     = props.getProperty("db.port",     "3306");
            String baseDeDonnees = props.getProperty("db.name", "gestion_notes");

            // Construction de l'URL JDBC
            // useSSL=false pour les serveurs sans certificat SSL
            // serverTimezone=UTC pour éviter les décalages horaires
            this.url = String.format(
                "jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8",
                hote, port, baseDeDonnees
            );

            this.utilisateur = props.getProperty("db.user",     "root");
            this.motDePasse  = props.getProperty("db.password", "");

            System.out.println("[ConnexionBDD] Configuration chargée → " + hote + ":" + port + "/" + baseDeDonnees);

        } catch (IOException e) {
            throw new RuntimeException(
                "[ConnexionBDD] Impossible de lire config.properties : " + e.getMessage(), e);
        }
    }

    /**
     * Ouvre la connexion JDBC vers le serveur MySQL.
     */
    private void ouvrirConnexion() {
        try {
            // Chargement explicite du driver (utile pour certains environnements)
            Class.forName("com.mysql.cj.jdbc.Driver");

            this.connection = DriverManager.getConnection(url, utilisateur, motDePasse);
            System.out.println("[ConnexionBDD] ✅ Connexion établie avec succès !");

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                "[ConnexionBDD] Driver MySQL introuvable. Vérifiez le pom.xml.", e);
        } catch (SQLException e) {
            throw new RuntimeException(
                "[ConnexionBDD] Échec de connexion MySQL : " + e.getMessage() +
                "\nVérifiez host/port/user/password dans config.properties.", e);
        }
    }
}