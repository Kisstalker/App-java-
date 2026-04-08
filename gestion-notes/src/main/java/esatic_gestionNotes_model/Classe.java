package esatic_gestionNotes_model;


/**
 * Modèle représentant une classe (ex : SIGL 2, IR 1).
 */
public class Classe {

    private int    id;
    private String libelle;

    //Constructeurs
    public Classe() {}

    public Classe(int id, String libelle) {
        this.id      = id;
        this.libelle = libelle;
    }

    //Getters / Setters
    public int    getId()             { return id; }
    public void   setId(int id)       { this.id = id; }

    public String getLibelle()               { return libelle; }
    public void   setLibelle(String libelle) { this.libelle = libelle; }

    // Utilisé automatiquement par les ComboBox JavaFX
    @Override
    public String toString() {
        return libelle;
    }
}