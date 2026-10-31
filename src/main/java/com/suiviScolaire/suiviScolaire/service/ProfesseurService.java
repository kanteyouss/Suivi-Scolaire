package com.suiviScolaire.suiviScolaire.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.suiviScolaire.suiviScolaire.entity.Classe;
import com.suiviScolaire.suiviScolaire.entity.Enseignement;
import com.suiviScolaire.suiviScolaire.entity.Matiere;
import com.suiviScolaire.suiviScolaire.entity.Professeur;
import com.suiviScolaire.suiviScolaire.repository.ClasseRepository;
import com.suiviScolaire.suiviScolaire.repository.EnseignementRepository;
import com.suiviScolaire.suiviScolaire.repository.MatiereRepository;
import com.suiviScolaire.suiviScolaire.repository.ProfesseurRepository;
import com.suiviScolaire.suiviScolaire.dto.MatiereResponseDto;
import com.suiviScolaire.suiviScolaire.dto.ProfesseurRequestDto;
import com.suiviScolaire.suiviScolaire.dto.ProfesseurResponseDto;
import com.suiviScolaire.suiviScolaire.dto.RequestDto;
import com.suiviScolaire.suiviScolaire.exception.DoublonRessourceException;
import com.suiviScolaire.suiviScolaire.exception.NotFoundException;
import com.suiviScolaire.suiviScolaire.exception.RessourceUtiliseeException;
import com.suiviScolaire.suiviScolaire.dto.ResponseDto;
import java.util.List;

/**
 * Service métier gérant les professeurs.
 * Fournit la création, la modification, la suppression et les conversions entité/DTO,
 * en remontant pour chaque professeur ses matières enseignées.
 */
@Service
public class ProfesseurService extends AbstractCrudService {

    private final ProfesseurRepository professeurRepository;
    private final EnseignementRepository enseignementRepository;
    private final MatiereRepository matiereRepository;
    private final ClasseRepository classeRepository;
    private final MatiereService matiereService;

    /**
     * Constructeur injectant le repository des professeurs, celui des enseignements et le service des matières.
     *
     * @param professeurRepository le repository des professeurs
     * @param enseignementRepository le repository des enseignements
     * @param matiereRepository le repository des matières
     * @param matiereService le service des matières (conversion)
     */
public ProfesseurService(ProfesseurRepository professeurRepository,
                         EnseignementRepository enseignementRepository,
                         MatiereRepository matiereRepository,
                         ClasseRepository classeRepository,
                         MatiereService matiereService) {
    super(professeurRepository);
    this.professeurRepository = professeurRepository;
    this.enseignementRepository = enseignementRepository;
    this.matiereRepository = matiereRepository;
    this.classeRepository = classeRepository;
    this.matiereService = matiereService;
}

    /**
     * Convertit les données d'une requête en entité professeur.
     *
     * @param professeurRequest Les données de la requête pour créer le professeur.
     * @return Professeur L'entité professeur créée.
     */
    public Professeur toEntity(ProfesseurRequestDto professeurRequest) {
        return Professeur.builder()
                .nom(professeurRequest.getNom())
                .prenom(professeurRequest.getPrenom())
                .email(professeurRequest.getEmail())
                .matiereId(professeurRequest.getMatiereId())
                .build();
    }

    /**
     * Convertit une entité professeur en objet de réponse, avec ses matières.
     *
     * @param entity L'entité professeur à convertir.
     * @return ResponseDto Les détails du professeur converti.
     */
    @Override
    public ResponseDto toDto(Object entity) {
        return toDto((Professeur) entity);
    }

    /**
     * Convertit une entité professeur en objet de réponse, avec sa matière principale
     * et ses matières enseignées.
     *
     * @param professeur L'entité professeur à convertir.
     * @return ProfesseurResponseDto Les détails du professeur converti.
     */
    public ProfesseurResponseDto toDto(Professeur professeur) {
        List<MatiereResponseDto> matieres = professeurRepository
                .findMatieresByProfesseur(professeur.getId())
                .stream()
                .map(matiere -> matiereService.toDto(matiere))
                .toList();
        return ProfesseurResponseDto.builder()
                .id(professeur.getId())
                .nom(professeur.getNom())
                .prenom(professeur.getPrenom())
                .email(professeur.getEmail())
                .matierePrincipale(resolveMatierePrincipale(professeur))
                .matieres(matieres)
                .build();
    }

    /**
     * Récupère une page de professeurs triés par nom.
     *
     * @param page l'index de la page souhaitée
     * @param taille le nombre d'éléments par page
     * @return une page d'objets {@link ResponseDto}
     */
    @Override
    public Page<ResponseDto> getAll(int page, int taille) {
        Pageable pageable = PageRequest.of(pageAutorisee(page), tailleAutorisee(taille), Sort.by("nom"));
        return professeurRepository.findAll(pageable).map(this::toDto);
    }

    /**
     * Crée un nouveau professeur après contrôle de l'unicité de l'email
     * et de l'existence de sa matière principale.
     *
     * @param requestDto les informations du professeur à créer
     * @return le professeur créé
     * @throws NotFoundException si la matière principale n'existe pas
     * @throws DoublonRessourceException si un professeur porte déjà cet email
     */
    @Override
    public ResponseDto create(RequestDto requestDto) {
        ProfesseurRequestDto professeurRequest = (ProfesseurRequestDto) requestDto;
        if (professeurRepository.existsByEmail(professeurRequest.getEmail())) {
            throw new DoublonRessourceException("Un professeur avec l'email '"
                    + professeurRequest.getEmail() + "' existe déjà.");
        }
        checkMatiereExiste(professeurRequest.getMatiereId());
        return toDto(professeurRepository.save(toEntity(professeurRequest)));
    }

    /**
     * Modifie un professeur existant (coordonnées et matière principale).
     *
     * @param id L'identifiant du professeur à modifier.
     * @param requestDto Les données de la requête pour modifier le professeur.
     * @return ResponseDto Les détails du professeur modifié.
     * @throws NotFoundException si le professeur ou la matière principale n'existe pas
     * @throws DoublonRessourceException si un autre professeur porte déjà cet email
     */
    @Override
    public ResponseDto update(Long id, RequestDto requestDto) {
        ProfesseurRequestDto professeurRequest = (ProfesseurRequestDto) requestDto;
        Professeur professeur = professeurRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MESSAGE));
        if (professeurRepository.existsByEmailAndIdNot(professeurRequest.getEmail(), id)) {
            throw new DoublonRessourceException("Un autre professeur avec l'email '"
                    + professeurRequest.getEmail() + "' existe déjà.");
        }
        checkMatiereExiste(professeurRequest.getMatiereId());
        professeur.setNom(professeurRequest.getNom());
        professeur.setPrenom(professeurRequest.getPrenom());
        professeur.setEmail(professeurRequest.getEmail());
        professeur.setMatiereId(professeurRequest.getMatiereId());
        return toDto(professeurRepository.save(professeur));
    }

    /**
     * Supprime un professeur, sauf s'il est affecté à une matière.
     *
     * @param id L'identifiant du professeur à supprimer.
     * @return ResponseDto Les détails du professeur supprimé.
     * @throws NotFoundException si le professeur n'existe pas
     * @throws RessourceUtiliseeException si le professeur est encore affecté
     */
    public ResponseDto delete(Long id) {
        Professeur professeur = professeurRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MESSAGE));
        if (enseignementRepository.existsByProfesseurId(id)) {
            throw new RessourceUtiliseeException("Professeur", id, "Enseignement");
        }
        professeurRepository.deleteById(id);
        return toDto(professeur);
    }

    /**
     * Vérifie qu'une matière existe.
     *
     * @param matiereId L'identifiant de la matière.
     * @throws NotFoundException si la matière n'existe pas
     */
    private void checkMatiereExiste(Long matiereId) {
        if (!matiereRepository.existsById(matiereId)) {
            throw new NotFoundException("Matière principale non trouvée");
        }
    }

    /**
     * Résout la matière principale du professeur (résolue par jointure).
     *
     * @param professeur Le professeur concerné.
     * @return La matière principale, ou null si le professeur n'en a pas.
     */
    private MatiereResponseDto resolveMatierePrincipale(Professeur professeur) {
        if (professeur.getMatiereId() == null) {
            return null;
        }
        return matiereRepository.findById(professeur.getMatiereId())
                .map(matiereService::toDto)
                .orElse(null);
    }

    /**
     * Affecte une matière à un professeur dans une classe.
     * Règle : une matière ne peut avoir qu'un seul professeur par classe.
     *
     * @param professeurId L'identifiant du professeur.
     * @param matiereId L'identifiant de la matière à affecter.
     * @param classeId L'identifiant de la classe.
     * @return ResponseDto du professeur affecté.
     * @throws NotFoundException si le professeur, la matière ou la classe n'existe pas
     * @throws DoublonRessourceException si un autre professeur est déjà affecté à cette matière dans cette classe (409)
     */
    public ResponseDto affecterMatiereAProfesseurDansClasse(Long professeurId, Long matiereId, Long classeId) {
        Professeur professeur = professeurRepository.findById(professeurId)
                .orElseThrow(() -> new NotFoundException("Professeur non trouvé"));
        Matiere matiere = matiereRepository.findById(matiereId)
                .orElseThrow(() -> new NotFoundException("Matière non trouvée"));
        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new NotFoundException("Classe non trouvée"));

        // Vérifier la règle : une matière ne peut avoir qu'un seul professeur par classe
        List<Enseignement> existants = enseignementRepository.findByMatiereIdAndClasseId(matiereId, classeId);
        for (Enseignement ens : existants) {
            if (!ens.getProfesseur().getId().equals(professeurId)) {
                Professeur autre = ens.getProfesseur();
                throw new DoublonRessourceException("Le professeur " + autre.getNom() + " " + autre.getPrenom()
                        + " est déjà affecté à la matière '" + matiere.getNom()
                        + "' dans la classe '" + classe.getNom() + "'.");
            }
        }
        List<Professeur> profsExistants = professeurRepository.findByMatiereIdAndClasseId(matiereId, classeId);
        for (Professeur autre : profsExistants) {
            if (!autre.getId().equals(professeurId)) {
                throw new DoublonRessourceException("Le professeur " + autre.getNom() + " " + autre.getPrenom()
                        + " est déjà affecté à la matière '" + matiere.getNom()
                        + "' dans la classe '" + classe.getNom() + "'.");
            }
        }

        if (existants.isEmpty()) {
            enseignementRepository.save(Enseignement.builder()
                    .professeur(professeur)
                    .matiere(matiere)
                    .classe(classe)
                    .build());
        }
        professeur.setMatiereId(matiereId);
        professeur.setClasseId(classeId);
        return toDto(professeurRepository.save(professeur));
    }

    /**
     * Recherche un professeur par son nom exact (premier résultat en cas d'homonymie).
     *
     * @param nom Le nom du professeur recherché.
     * @return ResponseDto Les détails du professeur trouvé.
     * @throws NotFoundException si aucun professeur ne porte ce nom
     */
    public ResponseDto getByNom(String nom) {
        Professeur professeur = professeurRepository.findByNom(nom).stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MESSAGE));
        return toDto(professeur);
    }
}
