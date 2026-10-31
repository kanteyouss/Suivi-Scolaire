package com.suiviScolaire.suiviScolaire.service;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import com.suiviScolaire.suiviScolaire.entity.Annee;
import com.suiviScolaire.suiviScolaire.repository.AnneeRepository;
import com.suiviScolaire.suiviScolaire.entity.Classe;
import com.suiviScolaire.suiviScolaire.repository.ClasseRepository;
import com.suiviScolaire.suiviScolaire.entity.Eleve;
import com.suiviScolaire.suiviScolaire.repository.EleveRepository;
import com.suiviScolaire.suiviScolaire.dto.ClasseElevesResponseDto;
import com.suiviScolaire.suiviScolaire.dto.EleveRequestDto;
import com.suiviScolaire.suiviScolaire.dto.EleveResponseDto;
import com.suiviScolaire.suiviScolaire.dto.RequestDto;
import com.suiviScolaire.suiviScolaire.exception.NotFoundException;
import com.suiviScolaire.suiviScolaire.exception.RequeteInvalideException;
import com.suiviScolaire.suiviScolaire.exception.RessourceUtiliseeException;
import com.suiviScolaire.suiviScolaire.exception.DoublonRessourceException;
import com.suiviScolaire.suiviScolaire.dto.ResponseDto;
import com.suiviScolaire.suiviScolaire.entity.Inscription;
import com.suiviScolaire.suiviScolaire.repository.InscriptionRepository;
import com.suiviScolaire.suiviScolaire.repository.NoteRepository;
import com.suiviScolaire.suiviScolaire.utils.response.Response;

/**
 * Service métier gérant les élèves.
 * Fournit la création, la modification, la suppression, les affectations de classe
 * et les conversions entité/DTO.
 */
@Service
public class EleveService extends AbstractCrudService {

    private final EleveRepository eleveRepository;
    private final ClasseRepository classeRepository;
    private final AnneeRepository anneeRepository;
    private final InscriptionRepository inscriptionRepository;
    private final NoteRepository noteRepository;
    private final ClasseService classeService;

    /**
     * Constructeur injectant les repositories et le service des classes.
     *
     * @param eleveRepository le repository des élèves
     * @param classeRepository le repository des classes
     * @param anneeRepository le repository des années scolaires
     * @param inscriptionRepository le repository des inscriptions
     * @param noteRepository le repository des notes
     * @param classeService le service des classes (conversion)
     */
    public EleveService(EleveRepository eleveRepository,
                        ClasseRepository classeRepository,
                        AnneeRepository anneeRepository,
                        InscriptionRepository inscriptionRepository,
                        NoteRepository noteRepository,
                        ClasseService classeService) {
        super(eleveRepository);
        this.eleveRepository = eleveRepository;
        this.classeRepository = classeRepository;
        this.anneeRepository = anneeRepository;
        this.inscriptionRepository = inscriptionRepository;
        this.noteRepository = noteRepository;
        this.classeService = classeService;
    }

    /**
     * Convertit une entité élève en objet de réponse.
     *
     * @param entity L'entité élève à convertir.
     * @return ResponseDto Les détails de l'élève converti.
     */
    @Override
    public ResponseDto toDto(Object entity) {
        return toDto((Eleve) entity);
    }

    /**
     * Convertit une entité élève en objet de réponse, avec sa classe
     * (la plus récente de ses inscriptions).
     *
     * @param eleve L'entité élève à convertir.
     * @return EleveResponseDto Les détails de l'élève converti.
     */
    public EleveResponseDto toDto(Eleve eleve) {
        EleveResponseDto dto = new EleveResponseDto();
        dto.setId(eleve.getId());
        dto.setMatricule(eleve.getMatricule());
        dto.setNom(eleve.getNom());
        dto.setPrenom(eleve.getPrenom());
        dto.setDateNaissance(eleve.getDateNaissance());
        Classe classeActuelle = classeActuelle(eleve);
        if (classeActuelle != null) {
            dto.setClasse(classeService.toDto(classeActuelle));
        }
        return dto;
    }

    /**
     * Récupère les élèves d'une classe, triés par nom puis prénom,
     * avec les effectifs de la classe.
     *
     * @param classeId L'identifiant de la classe.
     * @return ClasseElevesResponseDto La classe, ses effectifs et ses élèves.
     * @throws NotFoundException si la classe n'existe pas
     */
    public ClasseElevesResponseDto getElevesByClasse(Long classeId) {
        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MESSAGE));
        List<EleveResponseDto> eleves = inscriptionRepository.findElevesByClasseId(classeId).stream()
                .map(this::toDto)
                .toList();
        return ClasseElevesResponseDto.builder()
                .classeId(classe.getId())
                .nom(classe.getNom())
                .niveau(classe.getNiveau())
                .effectifMax(classe.getEffectifMax())
                .effectifReel(classeRepository.countElevesByClasse(classeId))
                .eleves(eleves)
                .build();
    }

    /**
     * Recherche des élèves dont le nom contient le fragment fourni (insensible à la casse).
     *
     * @param nom Le fragment de nom recherché.
     * @return La liste des élèves correspondants, avec leur classe.
     */
    public List<ResponseDto> searchByNom(String nom) {
        return eleveRepository.findByNomContainingIgnoreCase(nom).stream()
                .<ResponseDto>map(this::toDto)
                .toList();
    }

    /**
     * Récupère la liste paginée des élèves ou effectue une recherche filtrée par nom.
     *
     * @param page Numéro de la page (commence à 0).
     * @param taille Nombre d'éléments par page.
     * @param nom Nom ou fragment de nom à rechercher (optionnel).
     * @return Réponse contenant les élèves ou la liste paginée.
     */
    public Response<ResponseDto> getAllOrSearch(int page, int taille, String nom) {
        if (nom != null) {
            if (nom.isBlank()) {
                throw new RequeteInvalideException("Le nom de recherche ne peut pas être vide.");
            }
            return new Response<>(searchByNom(nom));
        }
        Page<ResponseDto> pageResult = getAll(page, taille);
        return new Response<>(pageResult.getContent(), pageResult.getTotalElements(), pageResult.getTotalPages(), page, taille);
    }

    /**
     * Recherche un eleve par son nom exact (premier résultat en cas d'homonymie).
     *
     * @param nom Le nom du eleve recherché.
     * @return ResponseDto Les détails du eleve trouvé.
     * @throws NotFoundException si aucun eleve ne porte ce nom
     */
    public ResponseDto getByNom(String nom) {
        Eleve eleve = eleveRepository.findByNom(nom).stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MESSAGE));
        return toDto(eleve);
    }

    /**
     * Crée un nouvel élève et, si une classe est fournie, son inscription
     * pour l'année scolaire indiquée (ou l'année la plus récente par défaut).
     *
     * @param requestDto les informations de l'élève à créer
     * @return l'élève créé
     * @throws NotFoundException si la classe ou l'année scolaire n'existe pas
     */
    @Override
    public ResponseDto create(RequestDto requestDto) {
        EleveRequestDto eleveRequest = (EleveRequestDto) requestDto;
        if (eleveRepository.existsByMatricule(eleveRequest.getMatricule())) {
            throw new DoublonRessourceException("Un élève avec le matricule '"
                    + eleveRequest.getMatricule() + "' existe déjà.");
        }
        if (eleveRequest.getClasseId() != null) {
            Classe classeCible = classeRepository.findById(eleveRequest.getClasseId())
                    .orElseThrow(() -> new NotFoundException("Classe non trouvée"));
            long effectifReel = classeRepository.countElevesByClasse(classeCible.getId());
            if (effectifReel >= classeCible.getEffectifMax()) {
                throw new IllegalArgumentException("La classe '" + classeCible.getNom()
                        + "' est pleine (" + effectifReel + "/" + classeCible.getEffectifMax() + ")");
            }
        }
        Eleve eleve = Eleve.builder()
                .matricule(eleveRequest.getMatricule())
                .nom(eleveRequest.getNom())
                .prenom(eleveRequest.getPrenom())
                .dateNaissance(eleveRequest.getDateNaissance())
                .build();
        eleve = eleveRepository.save(eleve);
        if (eleveRequest.getClasseId() != null) {
            changeClasse(eleve, eleveRequest.getClasseId(), eleveRequest.getAnneeId());
        }
        return toDto(eleve);
    }

    /**
     * Modifie un élève existant. La date de naissance reste figée ; la classe peut changer
     * si la classe de destination respecte le niveau et n'est pas pleine.
     *
     * @param id L'identifiant de l'élève à modifier.
     * @param requestDto Les données de la requête pour modifier l'élève.
     * @return ResponseDto Les détails de l'élève modifié.
     * @throws NotFoundException si l'élève ou la classe n'existe pas
     * @throws RequeteInvalideException si la classe de destination n'est pas du même niveau
     * @throws IllegalArgumentException si la classe de destination est pleine
     */
    @Override
    public ResponseDto update(Long id, RequestDto requestDto) {
        EleveRequestDto eleveRequest = (EleveRequestDto) requestDto;
        Eleve eleve = eleveRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MESSAGE));
        if (!eleveRequest.getDateNaissance().equals(eleve.getDateNaissance())) {
            throw new RequeteInvalideException("La date de naissance est figée et ne peut pas être modifiée.");
        }
        if (eleveRepository.existsByMatriculeAndIdNot(eleveRequest.getMatricule(), id)) {
            throw new DoublonRessourceException("Un autre élève avec le matricule '"
                    + eleveRequest.getMatricule() + "' existe déjà.");
        }
        eleve.setMatricule(eleveRequest.getMatricule());
        eleve.setNom(eleveRequest.getNom());
        eleve.setPrenom(eleveRequest.getPrenom());
        eleveRepository.save(eleve);
        if (eleveRequest.getClasseId() != null) {
            Classe classeCible = classeRepository.findById(eleveRequest.getClasseId())
                    .orElseThrow(() -> new NotFoundException("Classe non trouvée"));
            verifierAffectationAutorisee(eleve, classeCible);
            changeClasse(eleve, eleveRequest.getClasseId(), eleveRequest.getAnneeId());
        }
        return toDto(eleve);
    }

    /**
     * F11: Affecte un élève à une nouvelle classe en cours d'année.
     * L'élève ne peut changer que pour une classe de même niveau et non pleine.
     * L'inscription existante est mise à jour : l'élève conserve ses notes.
     *
     * @param eleveId L'identifiant de l'élève.
     * @param nouvelleClasseId L'identifiant de la classe de destination.
     * @param anneeId L'identifiant de l'année scolaire (optionnel : année la plus récente par défaut).
     * @return ResponseDto de l'élève affecté.
     * @throws NotFoundException si l'élève ou la classe de destination n'existe pas
     * @throws RequeteInvalideException si la classe de destination n'est pas du même niveau (400)
     * @throws IllegalArgumentException si la classe de destination est pleine (409)
     */
    public ResponseDto affecterEleveClasse(Long eleveId, Long nouvelleClasseId, Long anneeId) {
        Eleve eleve = eleveRepository.findById(eleveId)
                .orElseThrow(() -> new NotFoundException("Élève non trouvé"));
        if (nouvelleClasseId == null) {
            throw new RequeteInvalideException("La classe de destination est obligatoire.");
        }
        Classe nouvelleClasse = classeRepository.findById(nouvelleClasseId)
                .orElseThrow(() -> new NotFoundException("Classe non trouvée"));
        verifierAffectationAutorisee(eleve, nouvelleClasse);
        changeClasse(eleve, nouvelleClasseId, anneeId);
        return toDto(eleve);
    }

    /**
     * Supprime un élève et ses inscriptions, sauf s'il possède des notes.
     *
     * @param id L'identifiant de l'élève à supprimer.
     * @return ResponseDto Les détails de l'élève supprimé.
     * @throws NotFoundException si l'élève n'existe pas
     * @throws RessourceUtiliseeException si l'élève possède des notes
     */
    public ResponseDto delete(Long id) {
        Eleve eleve = eleveRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MESSAGE));
        if (noteRepository.countNotesByEleve(eleve.getId()) > 0) {
            throw new RessourceUtiliseeException("Élève", id, "Note");
        }
        inscriptionRepository.deleteAll(inscriptionRepository.findByEleveId(eleve.getId()));
        eleveRepository.delete(eleve);
        return toDto(eleve);
    }

    /**
     * Vérifie que l'élève peut être affecté à la classe cible :
     * même niveau que sa classe actuelle (si elle existe) et classe non pleine.
     *
     * @param eleve L'élève concerné.
     * @param classeCible La classe de destination.
     * @throws RequeteInvalideException si la classe de destination n'est pas du même niveau
     * @throws IllegalArgumentException si la classe de destination est pleine
     */
    private void verifierAffectationAutorisee(Eleve eleve, Classe classeCible) {
        Classe classeActuelle = classeActuelle(eleve);
        if (classeActuelle != null && !classeActuelle.getNiveau().equals(classeCible.getNiveau())) {
            throw new RequeteInvalideException("Niveau différent : l'élève est au niveau "
                    + classeActuelle.getNiveau() + ", la classe cible est au niveau " + classeCible.getNiveau());
        }
        long effectifReel = classeRepository.countElevesByClasse(classeCible.getId());
        if (effectifReel >= classeCible.getEffectifMax()) {
            throw new IllegalArgumentException("La classe '" + classeCible.getNom()
                    + "' est pleine (" + effectifReel + "/" + classeCible.getEffectifMax() + ")");
        }
    }

    /**
     * Change la classe de l'élève en conservant son inscription existante et ses notes.
     * L'ancienne inscription conserve ses dates ; une nouvelle inscription est créée
     * avec la nouvelle classe.
     *
     * @param eleve L'élève dont on change la classe.
     * @param nouvelleClasseId L'identifiant de la nouvelle classe.
     * @param anneeId L'identifiant de l'année scolaire (optionnel).
     * @throws NotFoundException si la classe ou l'année scolaire n'existe pas
     */
    private void changeClasse(Eleve eleve, Long nouvelleClasseId, Long anneeId) {
        Classe classe = classeRepository.findById(nouvelleClasseId)
                .orElseThrow(() -> new NotFoundException("Classe non trouvée"));
        Annee annee = resoudreAnnee(anneeId);
        List<Inscription> inscriptions = inscriptionRepository.findByEleveId(eleve.getId());
        for (Inscription ancienne : inscriptions) {
            if (ancienne.getDateFin() == null) {
                ancienne.setDateFin(LocalDate.now());
                inscriptionRepository.save(ancienne);
            }
        }
        inscriptionRepository.save(Inscription.builder()
                .eleve(eleve)
                .classe(classe)
                .annee(annee)
                .dateDebut(LocalDate.now())
                .build());
    }

    /**
     * Résout l'année scolaire à utiliser : celle fournie, sinon la plus récente.
     *
     * @param anneeId L'identifiant de l'année scolaire (peut être null).
     * @return Annee L'année scolaire résolue.
     * @throws NotFoundException si l'année demandée n'existe pas ou si aucune année n'est en base
     */
    private Annee resoudreAnnee(Long anneeId) {
        if (anneeId != null) {
            return anneeRepository.findById(anneeId)
                    .orElseThrow(() -> new NotFoundException("Année scolaire non trouvée"));
        }
        return anneeRepository.findTopByOrderByIdDesc()
                .orElseThrow(() -> new NotFoundException("Aucune année scolaire en base"));
    }

    /**
     * Récupère la classe actuelle de l'élève (inscription de l'année la plus récente).
     *
     * @param eleve L'élève concerné.
     * @return Classe La classe actuelle, ou null si l'élève n'est inscrit nulle part.
     */
    private Classe classeActuelle(Eleve eleve) {
        return eleveRepository.findClassesByEleve(eleve.getId()).stream()
                .findFirst()
                .orElse(null);
    }

    /**
     * Récupère tous les élèves, triés par nom puis prénom.
     *
     * @param page L'identifiant de la page.
     * @param taille La taille de la page.
     * @return Page<ResponseDto> La page d'élèves.
     */
    @Override
    public Page<ResponseDto> getAll(int page, int taille) {
        Pageable pageable = PageRequest.of(pageAutorisee(page), tailleAutorisee(taille), Sort.by("nom").and(Sort.by("prenom")));
        return eleveRepository.findAll(pageable).map(this::toDto);
    }
}
