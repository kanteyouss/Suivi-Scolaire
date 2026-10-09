package com.suiviScolaire.suiviScolaire.service;

import org.springframework.stereotype.Service;

import com.suiviScolaire.suiviScolaire.dto.RequestDto;
import com.suiviScolaire.suiviScolaire.dto.ResponseDto;
import com.suiviScolaire.suiviScolaire.entity.Classe;
import com.suiviScolaire.suiviScolaire.repository.ClasseRepository;
import com.suiviScolaire.suiviScolaire.dto.ClasseRequestDto;
import com.suiviScolaire.suiviScolaire.dto.ClasseResponseDto;

/**
 * Service métier gérant les classes.
 * Fournit la création, la modification et les conversions entité/DTO.
 * La lecture et la suppression sont héritées de {@link AbstractCrudService}.
 */
@Service
public class ClasseService extends AbstractCrudService {

    private final ClasseRepository classeRepository;

    /**
     * Constructeur injectant le repository des classes.
     *
     * @param classeRepository le repository des classes
     */
    public ClasseService(ClasseRepository classeRepository) {
        super(classeRepository);
        this.classeRepository = classeRepository;
    }

    /**
     * Convertit les données d'une requête en entité classe.
     *
     * @param classeRequest Les données de la requête pour créer la classe.
     * @return Classe L'entité classe créée.
     */
    public Classe toEntity(ClasseRequestDto classeRequest) {
        return Classe.builder()
                .nom(classeRequest.getNom())
                .niveau(classeRequest.getNiveau())
                .effectifMax(classeRequest.getEffectifMax())
                .build();
    }

    /**
     * Convertit une entité classe en objet de réponse, en calculant son effectif réel.
     *
     * @param entity L'entité classe à convertir.
     * @return ResponseDto Les détails de la classe convertie.
     */
    @Override
    public ResponseDto toDto(Object entity) {
        return toDto((Classe) entity);
    }

    /**
     * Convertit une entité classe en objet de réponse, en calculant son effectif réel.
     *
     * @param classe L'entité classe à convertir.
     * @return ClasseResponseDto Les détails de la classe convertie.
     */
    public ClasseResponseDto toDto(Classe classe) {
        long effectifReel = classeRepository.countElevesByClasse(classe.getId());
        return ClasseResponseDto.builder()
                .id(classe.getId())
                .nom(classe.getNom())
                .niveau(classe.getNiveau())
                .effectifMax(classe.getEffectifMax())
                .effectifReel(effectifReel)
                .build();
    }

    /**
     * Crée une nouvelle classe.
     *
     * @param requestDto les informations de la classe à créer
     * @return la classe créée
     */
    @Override
    public ResponseDto create(RequestDto requestDto) {
        ClasseRequestDto classeRequest = (ClasseRequestDto) requestDto;
        return toDto(classeRepository.save(toEntity(classeRequest)));
    }

    /**
     * Modifie une classe existante.
     *
     * @param id L'identifiant de la classe à modifier.
     * @param requestDto Les données de la requête pour modifier la classe.
     * @return ResponseDto Les détails de la classe modifiée.
     */
    @Override
    public ResponseDto update(Long id, RequestDto requestDto) {
        ClasseRequestDto classeRequest = (ClasseRequestDto) requestDto;
        Classe classe = classeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée"));
        classe.setNom(classeRequest.getNom());
        classe.setNiveau(classeRequest.getNiveau());
        classe.setEffectifMax(classeRequest.getEffectifMax());
        return toDto(classeRepository.save(classe));
    }
}
