package com.suiviScolaire.suiviScolaire.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.suiviScolaire.suiviScolaire.dto.RequestDto;
import com.suiviScolaire.suiviScolaire.exception.DoublonRessourceException;
import com.suiviScolaire.suiviScolaire.exception.NotFoundException;
import com.suiviScolaire.suiviScolaire.exception.RessourceUtiliseeException;
import com.suiviScolaire.suiviScolaire.dto.ResponseDto;
import com.suiviScolaire.suiviScolaire.entity.Matiere;
import com.suiviScolaire.suiviScolaire.repository.EnseignementRepository;
import com.suiviScolaire.suiviScolaire.repository.MatiereRepository;
import com.suiviScolaire.suiviScolaire.repository.NoteRepository;
import com.suiviScolaire.suiviScolaire.repository.PeriodeRepository;
import com.suiviScolaire.suiviScolaire.dto.MatiereRequestDto;
import com.suiviScolaire.suiviScolaire.dto.MatiereResponseDto;

/**
 * Service métier gérant les matières.
 * Fournit la création, la modification, la suppression et les conversions entité/DTO.
 */
@Service
public class MatiereService extends AbstractCrudService {

    private final MatiereRepository matiereRepository;
    private final NoteRepository noteRepository;
    private final EnseignementRepository enseignementRepository;
    private final PeriodeRepository periodeRepository;

    /**
     * Constructeur injectant les repositories des matières, notes, enseignements et périodes.
     *
     * @param matiereRepository le repository des matières
     * @param noteRepository le repository des notes
     * @param enseignementRepository le repository des enseignements
     * @param periodeRepository le repository des périodes
     */
    public MatiereService(MatiereRepository matiereRepository,
                          NoteRepository noteRepository,
                          EnseignementRepository enseignementRepository,
                          PeriodeRepository periodeRepository) {
        super(matiereRepository);
        this.matiereRepository = matiereRepository;
        this.noteRepository = noteRepository;
        this.enseignementRepository = enseignementRepository;
        this.periodeRepository = periodeRepository;
    }

    /**
     * Convertit les données d'une requête en entité matière.
     *
     * @param matiereRequest Les données de la requête pour créer la matière.
     * @return Matiere L'entité matière créée.
     */
    public Matiere toEntity(MatiereRequestDto matiereRequest) {
        return Matiere.builder()
                .nom(matiereRequest.getLibelle())
                .coefficient(matiereRequest.getCoefficient())
                .build();
    }

    /**
     * Convertit une entité matière en objet de réponse.
     *
     * @param entity L'entité matière à convertir.
     * @return ResponseDto Les détails de la matière convertie.
     */
    @Override
    public ResponseDto toDto(Object entity) {
        return toDto((Matiere) entity);
    }

    /**
     * Convertit une entité matière en objet de réponse.
     *
     * @param matiere L'entité matière à convertir.
     * @return MatiereResponseDto Les détails de la matière convertie.
     */
    public MatiereResponseDto toDto(Matiere matiere) {
        return MatiereResponseDto.builder()
                .id(matiere.getId())
                .nom(matiere.getNom())
                .coefficient(matiere.getCoefficient())
                .build();
    }

    /**
     * Récupère une page de matières triées par libellé.
     *
     * @param page l'index de la page souhaitée
     * @param taille le nombre d'éléments par page
     * @return une page d'objets {@link ResponseDto}
     */
    @Override
    public Page<ResponseDto> getAll(int page, int taille) {
        Pageable pageable = PageRequest.of(pageAutorisee(page), tailleAutorisee(taille), Sort.by("nom"));
        return matiereRepository.findAll(pageable).map(this::toDto);
    }

    /**
     * Crée une nouvelle matière après contrôle de l'unicité de son libellé.
     *
     * @param requestDto les informations de la matière à créer
     * @return la matière créée
     * @throws DoublonRessourceException si une matière porte déjà ce libellé
     */
    @Override
    public ResponseDto create(RequestDto requestDto) {
        MatiereRequestDto matiereRequest = (MatiereRequestDto) requestDto;
        if (matiereRepository.existsByNom(matiereRequest.getLibelle())) {
            throw new DoublonRessourceException("Une matière '" + matiereRequest.getLibelle() + "' existe déjà.");
        }
        return toDto(matiereRepository.save(toEntity(matiereRequest)));
    }

    /**
     * Modifie une matière existante. Le coefficient n'est plus modifiable
     * dès qu'un trimestre est clôturé.
     *
     * @param id L'identifiant de la matière à modifier.
     * @param requestDto Les données de la requête pour modifier la matière.
     * @return ResponseDto Les détails de la matière modifiée.
     * @throws NotFoundException si la matière n'existe pas
     * @throws IllegalArgumentException si un trimestre est clôturé
     */
    @Override
    public ResponseDto update(Long id, RequestDto requestDto) {
        MatiereRequestDto matiereRequest = (MatiereRequestDto) requestDto;
        Matiere matiere = matiereRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MESSAGE));
        if (periodeRepository.existsByClotureTrue()) {
            throw new IllegalArgumentException("Le coefficient n'est plus modifiable : un trimestre est clôturé.");
        }
        matiere.setNom(matiereRequest.getLibelle());
        matiere.setCoefficient(matiereRequest.getCoefficient());
        return toDto(matiereRepository.save(matiere));
    }

    /**
     * Supprime une matière, sauf si elle possède des notes ou est affectée à un professeur.
     *
     * @param id L'identifiant de la matière à supprimer.
     * @return ResponseDto Les détails de la matière supprimée.
     * @throws NotFoundException si la matière n'existe pas
     * @throws RessourceUtiliseeException si la matière a des notes ou un professeur affecté
     */
    public ResponseDto delete(Long id) {
        Matiere matiere = matiereRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MESSAGE));
        if (noteRepository.countNotesByMatiere(id) > 0) {
            throw new RessourceUtiliseeException("Matiere", id, "Note");
        }
        if (enseignementRepository.existsByMatiereId(id)) {
            throw new RessourceUtiliseeException("Matiere", id, "Professeur");
        }
        matiereRepository.deleteById(id);
        return toDto(matiere);
    }

    /**
     * Recherche un matiere par son nom exact (premier résultat en cas d'homonymie).
     *
     * @param nom Le nom du matiere recherché.
     * @return ResponseDto Les détails du matiere trouvé.
     * @throws NotFoundException si aucun matiere ne porte ce nom
     */
    public ResponseDto getByNom(String nom) {
        Matiere matiere = matiereRepository.findByNom(nom).stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MESSAGE));
        return toDto(matiere);
    }
}
