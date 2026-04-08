package esatic_gestionNotes_model;

/**
 * Modèle représentant un cours dispensé par un enseignant pour une classe.
 */
public class Cours {

    private int       id;
    private String    nom;
    private int       volumeHoraire;
    private String    formuleCalcul;   // ex: "MOYENNE_PONDEREE"
    private Enseignant enseignant;
    private Classe    classe;

    //Constructeurs
    public Cours() {}

    public Cours(int id, String nom, int volumeHoraire,
                 String formuleCalcul, Enseignant enseignant, Classe classe) {
        this.id             = id;
        this.nom            = nom;
        this.volumeHoraire  = volumeHoraire;
        this.formuleCalcul  = formuleCalcul;
        this.enseignant     = enseignant;
        this.classe         = classe;
    }

    //Getters / Setters
    public int    getId()          { return id; }
    public void   setId(int id)    { this.id = id; }

    public String getNom()              { return nom; }
    public void   setNom(String nom)    { this.nom = nom; }

    public int  getVolumeHoraire()                 { return volumeHoraire; }
    public void setVolumeHoraire(int volumeHoraire){ this.volumeHoraire = volumeHoraire; }

    public String getFormuleCalcul()                     { return formuleCalcul; }
    public void   setFormuleCalcul(String formuleCalcul) { this.formuleCalcul = formuleCalcul; }

    public Enseignant getEnseignant()                    { return enseignant; }
    public void       setEnseignant(Enseignant enseignant){ this.enseignant = enseignant; }

    public Classe getClasse()              { return classe; }
    public void   setClasse(Classe classe) { this.classe = classe; }

    @Override
    public String toString() {
        return nom + " — " + (classe != null ? classe.getLibelle() : "");
    }
}