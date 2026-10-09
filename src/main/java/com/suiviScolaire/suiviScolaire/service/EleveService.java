package com.suiviScolaire.suiviScolaire.service;

import org.springframework.stereotype.Service;

import java.util.List;
import com.suiviScolaire.suiviScolaire.entity.Annee;
import com.suiviScolaire.suiviScolaire.repository.AnneeRepository;
import com.suiviScolaire.suiviScolaire.entity.Classe;
import com.suiviScolaire.suiviScolaire.repository.ClasseRepository;
import com.suiviScolaire.suiviScolaire.entity.Eleve;
import com.suiviScolaire.suiviScolaire.repository.EleveRepository;
import com.suiviScolaire.suiviScolaire.dto.EleveRequestDto;
import com.suiviScolaire.suiviScolaire.dto.EleveResponseDto;
import com.suiviScolaire.suiviScolaire.dto.RequestDto;
import com.suiviScolaire.suiviScolaire.dto.ResponseDto;
import com.suiviScolaire.suiviScolaire.entity.Inscription;
import com.suiviScolaire.suiviScolaire.repository.InscriptionRepository;

/**
 * Service métier gérant les élèves.
 * Fournit la création, la modification, la suppression et les conversions entité/DTO.
 * Le rattachement à une classe passe par la table "inscription"
 * (sans relation directe entre Eleve et Classe).
 */
@Service
public class EleveService extends AbstractCrudService {

    private final EleveRepository eleveRepository;
    private final InscriptionRepository inscriptionRepository;
    private final ClasseRepository classeRepository;
    private final AnneeRepository anneeRepository;
    private final ClasseService classeService;

    /**
     * Constructeur injectant les repositories et le service des classes.
     *
     * @param eleveRepository le repository des élèves
     * @param inscriptionRepository le repository des inscriptions
     * @param classeRepository le repository des classes
     * @param anneeRepository le repository des années scolaires
     * @param classeService le service des classes (conversion)
     */
    public EleveService(EleveRepository eleveRepository,
                        InscriptionRepository inscriptionRepository,
                        ClasseRepository classeRepository,
                        AnneeRepository anneeRepository,
                        ClasseService classeService) {
        super(eleveRepository);
        this.eleveRepository = eleveRepository;
        this.inscriptionRepository = inscriptionRepository;
        this.classeRepository = classeRepository;
        this.anneeRepository = anneeRepository;
        this.classeService = classeService;
    }

    /**
     * Convertit les données d'une requête en entité élève.
     *
     * @param eleveRequest Les données de la requête pour créer l'élève.
     * @return Eleve L'entité élève créée.
     */
    public Eleve toEntity(EleveRequestDto eleveRequest) {
        return Eleve.builder()
                .nom(eleveRequest.getNom())
                .prenom(eleveRequest.getPrenom())
                .dateNaissance(eleveRequest.getDateNaissance())
                .build();
    }

    /**
     * Convertit une entité élève en objet de réponse, avec sa classe.
     *
     * @param entity L'entité élève à convertir.
     * @return ResponseDto Les détails de l'élève converti.
     */
    @Override
    public ResponseDto toDto(Object entity) {
        return toDto((Eleve) entity);
    }

    /**
     * Convertit une entité élève en objet de réponse, avec sa classe.
     *
     * @param eleve L'entité élève à convertir.
     * @return EleveResponseDto Les détails de l'élève converti.
     */
    public EleveResponseDto toDto(Eleve eleve) {
        Classe classe = eleveRepository.findClassesByEleve(eleve.getId())
                .stream().findFirst().orElse(null);
        return EleveResponseDto.builder()
                .id(eleve.getId())
                .nom(eleve.getNom())
                .prenom(eleve.getPrenom())
                .dateNaissance(eleve.getDateNaissance())
                .classe(classe == null ? null : classeService.toDto(classe))
                .build();
    }

    /**
     * Inscrit un nouvel élève et le rattache à une classe (si fournie).
     *
     * @param requestDto les informations de l'élève à créer
     * @return l'élève créé
     */
    @Override
    public ResponseDto create(RequestDto requestDto) {
        EleveRequestDto eleveRequest = (EleveRequestDto) requestDto;
        Eleve eleve = eleveRepository.save(toEntity(eleveRequest));
        if (eleveRequest.getClasseId() != null) {
            Classe classe = classeRepository.findById(eleveRequest.getClasseId())
                    .orElseThrow(() -> new RuntimeException("Classe non trouvée"));
            Annee annee = resolveAnnee(eleveRequest.getAnneeId());
            inscriptionRepository.save(Inscription.builder()
                    .eleve(eleve)
                    .classe(classe)
                    .annee(annee)
                    .build());
        }
        return toDto(eleve);
    }

    /**
     * Modifie un élève existant et, si une classe est fournie, change sa classe.
     *
     * @param id L'identifiant de l'élève à modifier.
     * @param requestDto Les données de la requête pour modifier l'élève.
     * @return ResponseDto Les détails de l'élève modifié.
     */
    @Override
    public ResponseDto update(Long id, RequestDto requestDto) {
        EleveRequestDto eleveRequest = (EleveRequestDto) requestDto;
        Eleve eleve = eleveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Élève non trouvé"));
        eleve.setNom(eleveRequest.getNom());
        eleve.setPrenom(eleveRequest.getPrenom());
        eleve.setDateNaissance(eleveRequest.getDateNaissance());
        eleveRepository.save(eleve);
        if (eleveRequest.getClasseId() != null) {
            changeClasse(eleve, eleveRequest.getClasseId(), eleveRequest.getAnneeId());
        }
        return toDto(eleve);
    }

    /**
     * Supprime un élève ainsi que ses inscriptions (évite la violation de clé étrangère).
     *
     * @param id L'identifiant de l'élève à supprimer.
     * @return ResponseDto Les détails de l'élève supprimé.
     * @throws Throwable si l'élève à supprimer n'existe pas
     */
    @Override
    public ResponseDto delete(Long id) throws Throwable {
        Eleve eleve = eleveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Élève non trouvé"));
        ResponseDto response = toDto(eleve);
        inscriptionRepository.deleteAll(inscriptionRepository.findByEleveId(id));
        eleveRepository.deleteById(id);
        return response;
    }

    /**
     * Change la classe d'un élève (met à jour son inscription, ou en crée une).
     *
     * @param eleve L'élève concerné.
     * @param classeId L'identifiant de la nouvelle classe.
     * @param anneeId L'identifiant de l'année (optionnel).
     */
    private void changeClasse(Eleve eleve, Long classeId, Long anneeId) {
        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée"));
        List<Inscription> inscriptions = inscriptionRepository.findByEleveId(eleve.getId());
        if (inscriptions.isEmpty()) {
            Annee annee = resolveAnnee(anneeId);
            inscriptionRepository.save(Inscription.builder()
                    .eleve(eleve)
                    .classe(classe)
                    .annee(annee)
                    .build());
        } else {
            Inscription inscription = inscriptions.get(0);
            inscription.setClasse(classe);
            inscriptionRepository.save(inscription);
        }
    }

    /**
     * Résout l'année scolaire à utiliser : celle fournie, sinon la plus récente.
     *
     * @param anneeId L'identifiant de l'année (peut être null).
     * @return Annee L'année résolue.
     */
    private Annee resolveAnnee(Long anneeId) {
        if (anneeId != null) {
            return anneeRepository.findById(anneeId)
                    .orElseThrow(() -> new RuntimeException("Année non trouvée"));
        }
        return anneeRepository.findTopByOrderByIdDesc()
                .orElseThrow(() -> new RuntimeException("Aucune année scolaire disponible"));
    }
}
