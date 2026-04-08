package esatic_gestionNotes_model;

/**
 * Modèle représentant un enseignant (utilisateur de l'application).
 */
public class Enseignant {

    private int    id;
    private String nom;
    private String prenoms;
    private String email;
    private String motDePasse;

    //Constructeurs
    public Enseignant() {}

    public Enseignant(int id, String nom, String prenoms, String email) {
        this.id      = id;
        this.nom     = nom;
        this.prenoms = prenoms;
        this.email   = email;
    }

    //Getters / Setters
    public int    getId()          { return id; }
    public void   setId(int id)    { this.id = id; }

    public String getNom()              { return nom; }
    public void   setNom(String nom)    { this.nom = nom; }

    public String getPrenoms()               { return prenoms; }
    public void   setPrenoms(String prenoms) { this.prenoms = prenoms; }

    public String getEmail()               { return email; }
    public void   setEmail(String email)   { this.email = email; }

    public String getMotDePasse()                  { return motDePasse; }
    public void   setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    @Override
    public String toString() {
        return prenoms + " " + nom;
    }
}