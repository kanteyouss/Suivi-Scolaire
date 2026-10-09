package com.suiviScolaire.suiviScolaire.service;

import org.springframework.stereotype.Service;

import com.suiviScolaire.suiviScolaire.entity.Enseignement;
import com.suiviScolaire.suiviScolaire.repository.EnseignementRepository;
import com.suiviScolaire.suiviScolaire.entity.Inscription;
import com.suiviScolaire.suiviScolaire.repository.InscriptionRepository;
import com.suiviScolaire.suiviScolaire.entity.Note;
import com.suiviScolaire.suiviScolaire.repository.NoteRepository;
import com.suiviScolaire.suiviScolaire.dto.NoteRequestDto;
import com.suiviScolaire.suiviScolaire.dto.NoteResponseDto;
import com.suiviScolaire.suiviScolaire.dto.RequestDto;
import com.suiviScolaire.suiviScolaire.dto.ResponseDto;
import com.suiviScolaire.suiviScolaire.entity.Periode;
import com.suiviScolaire.suiviScolaire.repository.PeriodeRepository;
import com.suiviScolaire.suiviScolaire.entity.TypeEvaluation;
import com.suiviScolaire.suiviScolaire.repository.TypeEvaluationRepository;

/**
 * Service métier gérant les notes.
 * Fournit les opérations CRUD ainsi que les conversions entité/DTO.
 * L'élève et la matière sont remontés via les relations existantes
 * (note → inscription → élève et note → enseignement → matière).
 */
@Service
public class NoteService extends AbstractCrudService {

    private final NoteRepository noteRepository;
    private final InscriptionRepository inscriptionRepository;
    private final EnseignementRepository enseignementRepository;
    private final PeriodeRepository periodeRepository;
    private final TypeEvaluationRepository typeEvaluationRepository;
    private final InscriptionService inscriptionService;
    private final EnseignementService enseignementService;
    private final PeriodeService periodeService;
    private final TypeEvaluationService typeEvaluationService;
    private final EleveService eleveService;
    private final MatiereService matiereService;

    /**
     * Constructeur injectant les repositories et les services de conversion.
     *
     * @param noteRepository le repository des notes
     * @param inscriptionRepository le repository des inscriptions
     * @param enseignementRepository le repository des enseignements
     * @param periodeRepository le repository des périodes
     * @param typeEvaluationRepository le repository des types d'évaluation
     * @param inscriptionService le service des inscriptions (conversion)
     * @param enseignementService le service des enseignements (conversion)
     * @param periodeService le service des périodes (conversion)
     * @param typeEvaluationService le service des types d'évaluation (conversion)
     * @param eleveService le service des élèves (conversion)
     * @param matiereService le service des matières (conversion)
     */
    public NoteService(NoteRepository noteRepository,
                       InscriptionRepository inscriptionRepository,
                       EnseignementRepository enseignementRepository,
                       PeriodeRepository periodeRepository,
                       TypeEvaluationRepository typeEvaluationRepository,
                       InscriptionService inscriptionService,
                       EnseignementService enseignementService,
                       PeriodeService periodeService,
                       TypeEvaluationService typeEvaluationService,
                       EleveService eleveService,
                       MatiereService matiereService) {
        super(noteRepository);
        this.noteRepository = noteRepository;
        this.inscriptionRepository = inscriptionRepository;
        this.enseignementRepository = enseignementRepository;
        this.periodeRepository = periodeRepository;
        this.typeEvaluationRepository = typeEvaluationRepository;
        this.inscriptionService = inscriptionService;
        this.enseignementService = enseignementService;
        this.periodeService = periodeService;
        this.typeEvaluationService = typeEvaluationService;
        this.eleveService = eleveService;
        this.matiereService = matiereService;
    }

    /**
     * Convertit les données d'une requête en entité note.
     *
     * @param noteRequest Les données de la requête pour créer la note.
     * @param inscription L'inscription de l'élève associée.
     * @param enseignement L'enseignement (professeur/matière) associé.
     * @param periode La période (trimestre) associée.
     * @param typeEvaluation Le type d'évaluation associé.
     * @return Note L'entité note créée.
     */
    public Note toEntity(NoteRequestDto noteRequest, Inscription inscription,
                         Enseignement enseignement, Periode periode,
                         TypeEvaluation typeEvaluation) {
        return Note.builder()
                .valeur(noteRequest.getValeur())
                .inscription(inscription)
                .enseignement(enseignement)
                .periode(periode)
                .typeEvaluation(typeEvaluation)
                .build();
    }

    /**
     * Convertit une entité note en objet de réponse, avec son élève et sa matière.
     *
     * @param entity L'entité note à convertir.
     * @return ResponseDto Les détails de la note convertie.
     */
    @Override
    public ResponseDto toDto(Object entity) {
        return toDto((Note) entity);
    }

    /**
     * Convertit une entité note en objet de réponse, avec son élève et sa matière.
     *
     * @param note L'entité note à convertir.
     * @return NoteResponseDto Les détails de la note convertie.
     */
    public NoteResponseDto toDto(Note note) {
        return NoteResponseDto.builder()
                .id(note.getId())
                .valeur(note.getValeur())
                .inscription(inscriptionService.toDto(note.getInscription()))
                .enseignement(enseignementService.toDto(note.getEnseignement()))
                .periode(periodeService.toDto(note.getPeriode()))
                .typeEvaluation(typeEvaluationService.toDto(note.getTypeEvaluation()))
                .eleve(eleveService.toDto(note.getInscription().getEleve()))
                .matiere(matiereService.toDto(note.getEnseignement().getMatiere()))
                .build();
    }

    /**
     * Crée une nouvelle note.
     *
     * @param requestDto les informations de la note à créer
     * @return la note créée
     */
    @Override
    public ResponseDto create(RequestDto requestDto) {
        NoteRequestDto noteRequest = (NoteRequestDto) requestDto;
        Inscription inscription = inscriptionRepository.findById(noteRequest.getInscriptionId())
                .orElseThrow(() -> new RuntimeException("Inscription non trouvée"));
        Enseignement enseignement = enseignementRepository.findById(noteRequest.getEnseignementId())
                .orElseThrow(() -> new RuntimeException("Enseignement non trouvé"));
        Periode periode = periodeRepository.findById(noteRequest.getPeriodeId())
                .orElseThrow(() -> new RuntimeException("Période non trouvée"));
        TypeEvaluation typeEvaluation = typeEvaluationRepository.findById(noteRequest.getTypeEvaluationId())
                .orElseThrow(() -> new RuntimeException("Type d'évaluation non trouvé"));
        return toDto(noteRepository.save(toEntity(noteRequest, inscription, enseignement, periode, typeEvaluation)));
    }

    /**
     * Modifie une note existante.
     *
     * @param id L'identifiant de la note à modifier.
     * @param requestDto Les données de la requête pour modifier la note.
     * @return ResponseDto Les détails de la note modifiée.
     */
    @Override
    public ResponseDto update(Long id, RequestDto requestDto) {
        NoteRequestDto noteRequest = (NoteRequestDto) requestDto;
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note non trouvée"));
        Inscription inscription = inscriptionRepository.findById(noteRequest.getInscriptionId())
                .orElseThrow(() -> new RuntimeException("Inscription non trouvée"));
        Enseignement enseignement = enseignementRepository.findById(noteRequest.getEnseignementId())
                .orElseThrow(() -> new RuntimeException("Enseignement non trouvé"));
        Periode periode = periodeRepository.findById(noteRequest.getPeriodeId())
                .orElseThrow(() -> new RuntimeException("Période non trouvée"));
        TypeEvaluation typeEvaluation = typeEvaluationRepository.findById(noteRequest.getTypeEvaluationId())
                .orElseThrow(() -> new RuntimeException("Type d'évaluation non trouvé"));
        note.setValeur(noteRequest.getValeur());
        note.setInscription(inscription);
        note.setEnseignement(enseignement);
        note.setPeriode(periode);
        note.setTypeEvaluation(typeEvaluation);
        return toDto(noteRepository.save(note));
    }
}
