package esatic_gestionNotes_model;


/**
 * Modèle représentant une évaluation d'un cours.
 * Ex : "Note 1" coeff 1, "DS" coeff 2...
 */
public class Evaluation {

    private int    id;
    private String libelle;
    private double coefficient;
    private int    idCours;

    //Constructeurs 
    public Evaluation() {}

    public Evaluation(int id, String libelle, double coefficient, int idCours) {
        this.id          = id;
        this.libelle     = libelle;
        this.coefficient = coefficient;
        this.idCours     = idCours;
    }

    //Getters / Setters
    public int    getId()          { return id; }
    public void   setId(int id)    { this.id = id; }

    public String getLibelle()               { return libelle; }
    public void   setLibelle(String libelle) { this.libelle = libelle; }

    public double getCoefficient()                 { return coefficient; }
    public void   setCoefficient(double coefficient){ this.coefficient = coefficient; }

    public int  getIdCours()             { return idCours; }
    public void setIdCours(int idCours)  { this.idCours = idCours; }

    @Override
    public String toString() {
        return libelle + " (coeff " + coefficient + ")";
    }
}