package com.suiviScolaire.suiviScolaire.service;

import org.springframework.stereotype.Service;

import java.util.List;
import com.suiviScolaire.suiviScolaire.dto.RequestDto;
import com.suiviScolaire.suiviScolaire.dto.ResponseDto;
import com.suiviScolaire.suiviScolaire.entity.Coefficient;
import com.suiviScolaire.suiviScolaire.dto.CoefficientResponseDto;
import com.suiviScolaire.suiviScolaire.entity.Matiere;
import com.suiviScolaire.suiviScolaire.repository.MatiereRepository;
import com.suiviScolaire.suiviScolaire.dto.MatiereRequestDto;
import com.suiviScolaire.suiviScolaire.dto.MatiereResponseDto;

/**
 * Service métier gérant les matières.
 * Fournit la création, la modification et les conversions entité/DTO,
 * en remontant pour chaque matière la liste de ses coefficients.
 */
@Service
public class MatiereService extends AbstractCrudService {

    private final MatiereRepository matiereRepository;
    private final CoefficientService coefficientService;

    /**
     * Constructeur injectant le repository des matières et le service des coefficients.
     *
     * @param matiereRepository le repository des matières
     * @param coefficientService le service des coefficients (conversion)
     */
    public MatiereService(MatiereRepository matiereRepository,
                          CoefficientService coefficientService) {
        super(matiereRepository);
        this.matiereRepository = matiereRepository;
        this.coefficientService = coefficientService;
    }

    /**
     * Convertit les données d'une requête en entité matière.
     *
     * @param matiereRequest Les données de la requête pour créer la matière.
     * @return Matiere L'entité matière créée.
     */
    public Matiere toEntity(MatiereRequestDto matiereRequest) {
        return Matiere.builder()
                .nom(matiereRequest.getNom())
                .build();
    }

    /**
     * Convertit une entité matière en objet de réponse, avec ses coefficients.
     *
     * @param entity L'entité matière à convertir.
     * @return ResponseDto Les détails de la matière convertie.
     */
    @Override
    public ResponseDto toDto(Object entity) {
        return toDto((Matiere) entity);
    }

    /**
     * Convertit une entité matière en objet de réponse, avec ses coefficients.
     *
     * @param matiere L'entité matière à convertir.
     * @return MatiereResponseDto Les détails de la matière convertie.
     */
    public MatiereResponseDto toDto(Matiere matiere) {
        List<CoefficientResponseDto> coefficients = matiereRepository
                .findCoefficientsByMatiere(matiere.getId())
                .stream()
                .map(coefficientService::toDto)
                .toList();
        return MatiereResponseDto.builder()
                .id(matiere.getId())
                .nom(matiere.getNom())
                .coefficients(coefficients)
                .build();
    }

    /**
     * Crée une nouvelle matière.
     *
     * @param requestDto les informations de la matière à créer
     * @return la matière créée
     */
    @Override
    public ResponseDto create(RequestDto requestDto) {
        MatiereRequestDto matiereRequest = (MatiereRequestDto) requestDto;
        return toDto(matiereRepository.save(toEntity(matiereRequest)));
    }

    /**
     * Modifie une matière existante.
     *
     * @param id L'identifiant de la matière à modifier.
     * @param requestDto Les données de la requête pour modifier la matière.
     * @return ResponseDto Les détails de la matière modifiée.
     */
    @Override
    public ResponseDto update(Long id, RequestDto requestDto) {
        MatiereRequestDto matiereRequest = (MatiereRequestDto) requestDto;
        Matiere matiere = matiereRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Matière non trouvée"));
        matiere.setNom(matiereRequest.getNom());
        return toDto(matiereRepository.save(matiere));
    }
}
