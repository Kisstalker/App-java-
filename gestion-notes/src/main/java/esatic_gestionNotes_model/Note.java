package esatic_gestionNotes_model;


/**
 * Modèle représentant la note d'un étudiant pour une évaluation donnée.
 * Inclut bonus et malus appliqués par l'enseignant.
 */
public class Note {

    private int     id;
    private Double  valeur;        // null = non saisie
    private double  bonus;
    private double  malus;
    private int     idEvaluation;
    private int     idEtudiant;

    //Constructeurs
    public Note() {}

    public Note(int id, Double valeur, double bonus,
                double malus, int idEvaluation, int idEtudiant) {
        this.id           = id;
        this.valeur       = valeur;
        this.bonus        = bonus;
        this.malus        = malus;
        this.idEvaluation = idEvaluation;
        this.idEtudiant   = idEtudiant;
    }

    //Getters / Setters
    public int    getId()          { return id; }
    public void   setId(int id)    { this.id = id; }

    public Double getValeur()              { return valeur; }
    public void   setValeur(Double valeur) { this.valeur = valeur; }

    public double getBonus()              { return bonus; }
    public void   setBonus(double bonus)  { this.bonus = bonus; }

    public double getMalus()              { return malus; }
    public void   setMalus(double malus)  { this.malus = malus; }

    public int  getIdEvaluation()                { return idEvaluation; }
    public void setIdEvaluation(int idEvaluation){ this.idEvaluation = idEvaluation; }

    public int  getIdEtudiant()              { return idEtudiant; }
    public void setIdEtudiant(int idEtudiant){ this.idEtudiant = idEtudiant; }

    /**
     * Retourne la valeur effective après application du bonus et du malus.
     * Résultat plafonné entre 0 et 20.
     */
    public double getValeurEffective() {
        if (valeur == null) return 0.0;
        double resultat = valeur + bonus - malus;
        return Math.max(0.0, Math.min(20.0, resultat));
    }

    /**
     * Indique si la note a été saisie (non nulle).
     */
    public boolean estSaisie() {
        return valeur != null;
    }

    @Override
    public String toString() {
        return valeur == null ? "—" : String.valueOf(valeur);
    }
}