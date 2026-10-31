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
import com.suiviScolaire.suiviScolaire.entity.Classe;
import com.suiviScolaire.suiviScolaire.repository.ClasseRepository;
import com.suiviScolaire.suiviScolaire.repository.InscriptionRepository;
import com.suiviScolaire.suiviScolaire.dto.ClasseRequestDto;
import com.suiviScolaire.suiviScolaire.dto.ClasseResponseDto;

/**
 * Service métier gérant les classes.
 * Fournit la création, la modification, la suppression et les conversions entité/DTO.
 * L'effectif réel est calculé par jointure sur la table des inscriptions.
 */
@Service
public class ClasseService extends AbstractCrudService {

    private final ClasseRepository classeRepository;
    private final InscriptionRepository inscriptionRepository;

    /**
     * Constructeur injectant le repository des classes et celui des inscriptions.
     *
     * @param classeRepository le repository des classes
     * @param inscriptionRepository le repository des inscriptions
     */
    public ClasseService(ClasseRepository classeRepository,
                         InscriptionRepository inscriptionRepository) {
        super(classeRepository);
        this.classeRepository = classeRepository;
        this.inscriptionRepository = inscriptionRepository;
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
     * Récupère une page de classes triées par niveau puis par nom.
     *
     * @param page l'index de la page souhaitée
     * @param taille le nombre d'éléments par page
     * @return une page d'objets {@link ResponseDto}
     */
    @Override
    public Page<ResponseDto> getAll(int page, int taille) {
        Pageable pageable = PageRequest.of(pageAutorisee(page), tailleAutorisee(taille),
                Sort.by("niveau", "nom"));
        return classeRepository.findAll(pageable).map(this::toDto);
    }

    /**
     * Crée une nouvelle classe après contrôle de l'unicité du nom dans le niveau.
     *
     * @param requestDto les informations de la classe à créer
     * @return la classe créée
     * @throws DoublonRessourceException si une classe de même nom existe déjà dans ce niveau
     */
    @Override
    public ResponseDto create(RequestDto requestDto) {
        ClasseRequestDto classeRequest = (ClasseRequestDto) requestDto;
        if (classeRepository.existsByNomAndNiveau(classeRequest.getNom(), classeRequest.getNiveau())) {
            throw new DoublonRessourceException("Une classe nommée '" + classeRequest.getNom()
                    + "' existe déjà au niveau '" + classeRequest.getNiveau() + "'.");
        }
        return toDto(classeRepository.save(toEntity(classeRequest)));
    }

    /**
     * Modifie une classe existante, en contrôlant que l'effectif maximum
     * n'est pas ramené sous l'effectif actuel.
     *
     * @param id L'identifiant de la classe à modifier.
     * @param requestDto Les données de la requête pour modifier la classe.
     * @return ResponseDto Les détails de la classe modifiée.
     * @throws NotFoundException si la classe n'existe pas
     * @throws DoublonRessourceException si une autre classe de même nom existe dans ce niveau
     * @throws IllegalArgumentException si l'effectif maximum est ramené sous l'effectif actuel
     */
    @Override
    public ResponseDto update(Long id, RequestDto requestDto) {
        ClasseRequestDto classeRequest = (ClasseRequestDto) requestDto;
        Classe classe = classeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MESSAGE));
        if (classeRepository.existsByNomAndNiveauAndIdNot(classeRequest.getNom(), classeRequest.getNiveau(), id)) {
            throw new DoublonRessourceException("Une autre classe nommée '" + classeRequest.getNom()
                    + "' existe déjà au niveau '" + classeRequest.getNiveau() + "'.");
        }
        long effectifReel = classeRepository.countElevesByClasse(id);
        if (classeRequest.getEffectifMax() < effectifReel) {
            throw new IllegalArgumentException("L'effectif maximum (" + classeRequest.getEffectifMax()
                    + ") ne peut pas être inférieur à l'effectif actuel (" + effectifReel + ").");
        }
        classe.setNom(classeRequest.getNom());
        classe.setNiveau(classeRequest.getNiveau());
        classe.setEffectifMax(classeRequest.getEffectifMax());
        return toDto(classeRepository.save(classe));
    }

    /**
     * Supprime une classe après vérification qu'elle ne compte aucun élève.
     *
     * @param id L'identifiant de la classe à supprimer.
     * @return ResponseDto Les détails de la classe supprimée.
     * @throws NotFoundException si la classe n'existe pas
     * @throws RessourceUtiliseeException si des élèves sont encore inscrits dans cette classe
     */
    public ResponseDto delete(Long id) {
        Classe classe = classeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MESSAGE));
        if (inscriptionRepository.existsByClasseId(id)) {
            throw new RessourceUtiliseeException("Classe", id, "Eleve");
        }
        classeRepository.deleteById(id);
        return toDto(classe);
    }

    /**
     * Recherche un classe par son nom exact (premier résultat en cas d'homonymie).
     *
     * @param nom Le nom du classe recherché.
     * @return ResponseDto Les détails du classe trouvé.
     * @throws NotFoundException si aucun classe ne porte ce nom
     */
    public ResponseDto getByNom(String nom) {
        Classe classe = classeRepository.findByNom(nom).stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MESSAGE));
        return toDto(classe);
    }
}
