package com.suiviScolaire.suiviScolaire.service;

import com.suiviScolaire.suiviScolaire.dto.ResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Service abstrait de base (classe mère) pour toutes les entités de l'application.
 * Il implémente {@link CrudService} et centralise la logique métier répétitive
 * de lecture et de suppression en utilisant un {@link JpaRepository} global (type brut).
 * Les classes enfants (ex: ClasseService, EleveService) héritent de ces méthodes
 * et se concentrent uniquement sur la création, la modification et le mapping (DTO/Entité).
 */
public abstract class AbstractCrudService implements CrudService{

    /**
     * Le repository global utilisé pour les opérations génériques (find, delete).
     * Il est injecté par la classe enfant via le mot-clé "super".
     */
    private final JpaRepository repository;

    /**
     * Constructeur protégé appelé par les services enfants lors de leur instanciation.
     *
     * @param repository le repository spécifique à l'entité (ex: ClasseRepository)
     *                   traité ici de manière globale
     */
    public AbstractCrudService(JpaRepository repository) {
        this.repository = repository;
    }

    /**
     * Récupère toutes les entités présentes dans la base de données
     * et les convertit en DTOs de réponse.
     *
     * @return une liste d'objets {@link ResponseDto}
     */
    public List<ResponseDto> getAll() {
        List<Object> entities = repository.findAll();
        return entities.stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Recherche une entité spécifique par son identifiant et la convertit en DTO.
     *
     * @param id l'identifiant unique de l'entité recherchée
     * @return un objet {@link ResponseDto} représentant l'entité trouvée
     * @throws RuntimeException si aucune entité ne correspond à cet identifiant
     * @throws Throwable pour gérer les exceptions génériques levées par le orElseThrow
     */
    public ResponseDto getById(Long id) throws Throwable {
        Object entity = repository.findById(id).orElseThrow(() -> new RuntimeException("Élément non trouvé"));
        return toDto(entity);
    }
    /**
     * Supprime une entité de la base de données après avoir vérifié son existence,
     * puis retourne les informations de l'entité supprimée.
     *
     * @param id l'identifiant de l'entité à supprimer
     * @return un objet {@link ResponseDto} contenant les données de l'entité tout juste supprimée
     * @throws RuntimeException si l'entité à supprimer n'existe pas
     * @throws Throwable pour gérer les exceptions génériques levées par le orElseThrow
     */
    public ResponseDto delete(Long id) throws Throwable {
        Object entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Élément non trouvé"));
        repository.deleteById(id);
        return toDto(entity);
    }
}
