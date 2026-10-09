package com.suiviScolaire.suiviScolaire.service;

import org.springframework.stereotype.Service;

import java.util.List;
import com.suiviScolaire.suiviScolaire.entity.Professeur;
import com.suiviScolaire.suiviScolaire.repository.ProfesseurRepository;
import com.suiviScolaire.suiviScolaire.dto.MatiereResponseDto;
import com.suiviScolaire.suiviScolaire.dto.ProfesseurRequestDto;
import com.suiviScolaire.suiviScolaire.dto.ProfesseurResponseDto;
import com.suiviScolaire.suiviScolaire.dto.RequestDto;
import com.suiviScolaire.suiviScolaire.dto.ResponseDto;

/**
 * Service métier gérant les professeurs.
 * Fournit la création, la modification et les conversions entité/DTO,
 * en remontant pour chaque professeur ses matières enseignées.
 */
@Service
public class ProfesseurService extends AbstractCrudService {

    private final ProfesseurRepository professeurRepository;
    private final MatiereService matiereService;

    /**
     * Constructeur injectant le repository des professeurs et le service des matières.
     *
     * @param professeurRepository le repository des professeurs
     * @param matiereService le service des matières (conversion)
     */
    public ProfesseurService(ProfesseurRepository professeurRepository, MatiereService matiereService) {
        super(professeurRepository);
        this.professeurRepository = professeurRepository;
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
     * Convertit une entité professeur en objet de réponse, avec ses matières enseignées.
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
                .matieres(matieres)
                .build();
    }

    /**
     * Crée un nouveau professeur.
     *
     * @param requestDto les informations du professeur à créer
     * @return le professeur créé
     */
    @Override
    public ResponseDto create(RequestDto requestDto) {
        ProfesseurRequestDto professeurRequest = (ProfesseurRequestDto) requestDto;
        return toDto(professeurRepository.save(toEntity(professeurRequest)));
    }

    /**
     * Modifie un professeur existant.
     *
     * @param id L'identifiant du professeur à modifier.
     * @param requestDto Les données de la requête pour modifier le professeur.
     * @return ResponseDto Les détails du professeur modifié.
     */
    @Override
    public ResponseDto update(Long id, RequestDto requestDto) {
        ProfesseurRequestDto professeurRequest = (ProfesseurRequestDto) requestDto;
        Professeur professeur = professeurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Professeur non trouvé"));
        professeur.setNom(professeurRequest.getNom());
        professeur.setPrenom(professeurRequest.getPrenom());
        professeur.setEmail(professeurRequest.getEmail());
        return toDto(professeurRepository.save(professeur));
    }
}
