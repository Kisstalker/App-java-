package esatic_gestionNotes_model;


/**
 * Modèle représentant un étudiant.
 */
public class Etudiant {

    private int    id;
    private String matricule;
    private String nom;
    private String prenoms;
    private String statut;     // "N" = Nouveau, "R" = Redoublant

    //Constructeurs 
    public Etudiant() {}

    public Etudiant(int id, String matricule, String nom,
                    String prenoms, String statut) {
        this.id        = id;
        this.matricule = matricule;
        this.nom       = nom;
        this.prenoms   = prenoms;
        this.statut    = statut;
    }

    //Getters / Setters 
    public int    getId()          { return id; }
    public void   setId(int id)    { this.id = id; }

    public String getMatricule()                 { return matricule; }
    public void   setMatricule(String matricule) { this.matricule = matricule; }

    public String getNom()              { return nom; }
    public void   setNom(String nom)    { this.nom = nom; }

    public String getPrenoms()               { return prenoms; }
    public void   setPrenoms(String prenoms) { this.prenoms = prenoms; }

    public String getStatut()              { return statut; }
    public void   setStatut(String statut) { this.statut = statut; }

    @Override
    public String toString() {
        return matricule + " — " + nom + " " + prenoms;
    }
}