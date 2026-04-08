package esatic_gestionNotes_service;


import esatic_gestionNotes_dao.ClasseDAO;
import esatic_gestionNotes_dao.CoursDAO;
import esatic_gestionNotes_dao.EtudiantDAO;
import esatic_gestionNotes_model.Classe;
import esatic_gestionNotes_model.Cours;
import esatic_gestionNotes_model.Etudiant;

import java.time.Year;
import java.util.List;

/**
 * Service métier pour la gestion des cours, classes et étudiants.
 */
public class CoursService {

    private final CoursDAO    coursDAO;
    private final ClasseDAO   classeDAO;
    private final EtudiantDAO etudiantDAO;

    public CoursService() {
        this.coursDAO    = new CoursDAO();
        this.classeDAO   = new ClasseDAO();
        this.etudiantDAO = new EtudiantDAO();
    }


    // COURS

    /**
     * Crée un cours pour un enseignant.
     * Génère automatiquement 2 évaluations par défaut.
     *
     * @throws IllegalArgumentException si les champs obligatoires sont vides
     */
    public boolean creerCours(Cours cours) {
        validerCours(cours);
        return coursDAO.creer(cours);
    }

    /**
     * Retourne tous les cours d'un enseignant connecté.
     */
    public List<Cours> getMesCours(int idEnseignant) {
        return coursDAO.listerParEnseignant(idEnseignant);
    }

    /**
     * Retourne un cours par son identifiant.
     */
    public Cours getCours(int idCours) {
        return coursDAO.trouverParId(idCours);
    }

    /**
     * Modifie les informations d'un cours existant.
     */
    public boolean modifierCours(Cours cours) {
        validerCours(cours);
        return coursDAO.modifier(cours);
    }

    /**
     * Supprime un cours et toutes ses données associées (cascade BDD).
     */
    public boolean supprimerCours(int idCours) {
        return coursDAO.supprimer(idCours);
    }

   
    // CLASSES

    /**
     * Retourne toutes les classes disponibles (pour les ComboBox).
     */
    public List<Classe> getClasses() {
        return classeDAO.listerToutes();
    }

    /**
     * Crée une nouvelle classe.
     */
    public boolean creerClasse(String libelle) {
        if (libelle == null || libelle.trim().isEmpty()) {
            throw new IllegalArgumentException("Le libellé de la classe est obligatoire.");
        }
        Classe classe = new Classe();
        classe.setLibelle(libelle.trim().toUpperCase());
        return classeDAO.creer(classe);
    }

   
    // ÉTUDIANTS

    /**
     * Retourne tous les étudiants d'un cours.
     */
    public List<Etudiant> getEtudiants(int idCours) {
        return etudiantDAO.listerParCours(idCours);
    }

    /**
     * Ajoute un étudiant et l'inscrit dans la classe du cours.
     *
     * @param etudiant  l'étudiant à créer
     * @param idClasse  la classe cible
     * @throws IllegalArgumentException si le matricule existe déjà ou les champs sont vides
     */
    public boolean ajouterEtudiant(Etudiant etudiant, int idClasse) {
        validerEtudiant(etudiant);

        if (etudiantDAO.matriculeExiste(etudiant.getMatricule())) {
            throw new IllegalArgumentException(
                "Le matricule « " + etudiant.getMatricule() + " » existe déjà.");
        }

        int anneeActuelle = Year.now().getValue();
        return etudiantDAO.creer(etudiant, idClasse, anneeActuelle);
    }

    /**
     * Modifie les informations d'un étudiant.
     */
    public boolean modifierEtudiant(Etudiant etudiant) {
        validerEtudiant(etudiant);
        return etudiantDAO.modifier(etudiant);
    }

    /**
     * Supprime un étudiant.
     */
    public boolean supprimerEtudiant(int idEtudiant) {
        return etudiantDAO.supprimer(idEtudiant);
    }

    
    // VALIDATIONS INTERNES

    private void validerCours(Cours cours) {
        if (cours.getNom() == null || cours.getNom().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du cours est obligatoire.");
        }
        if (cours.getClasse() == null) {
            throw new IllegalArgumentException("La classe du cours est obligatoire.");
        }
        if (cours.getEnseignant() == null) {
            throw new IllegalArgumentException("L'enseignant du cours est obligatoire.");
        }
    }

    private void validerEtudiant(Etudiant etudiant) {
        if (etudiant.getMatricule() == null || etudiant.getMatricule().trim().isEmpty()) {
            throw new IllegalArgumentException("Le matricule est obligatoire.");
        }
        if (etudiant.getNom() == null || etudiant.getNom().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom est obligatoire.");
        }
        if (etudiant.getPrenoms() == null || etudiant.getPrenoms().trim().isEmpty()) {
            throw new IllegalArgumentException("Les prénoms sont obligatoires.");
        }
    }
}