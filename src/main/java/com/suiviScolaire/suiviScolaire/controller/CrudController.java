package com.suiviScolaire.suiviScolaire.controller;

import com.suiviScolaire.suiviScolaire.dto.RequestDto;
import com.suiviScolaire.suiviScolaire.dto.ResponseDto;
import com.suiviScolaire.suiviScolaire.service.AbstractCrudService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur abstrait centralisant les routes HTTP pour les opérations CRUD de base.
 * Cette classe permet de factoriser le code des contrôleurs spécifiques de l'application
 * en délégant le traitement à un service de type {@link AbstractCrudService}.
 */
public abstract class CrudController {

    private final AbstractCrudService service;

    /**
     * Constructeur injectant le service métier requis pour les opérations.
     *
     * @param service le service métier qui gère la logique CRUD pour l'entité concernée
     */
    public CrudController(AbstractCrudService service) {
        this.service = service;
    }

    /**
     * Récupère la liste de toutes les ressources.
     *
     * @return une liste d'objets {@link ResponseDto} représentant les ressources
     */
    @GetMapping
    public List<ResponseDto> getAll() {
        return service.getAll();
    }

    /**
     * Récupère une ressource spécifique à partir de son identifiant.
     *
     * @param id l'identifiant unique de la ressource à récupérer
     * @return un objet {@link ResponseDto} représentant la ressource trouvée
     * @throws Throwable si la ressource n'existe pas en base de données
     */
    @GetMapping("/{id}")
    public ResponseDto getById(@PathVariable Long id) throws Throwable  {
        return service.getById(id);
    }

    /**
     * Crée une nouvelle ressource en base de données.
     *
     * @param request l'objet {@link RequestDto} contenant les informations de la ressource à créer
     * @return un objet {@link ResponseDto} représentant la ressource nouvellement créée
     */
    @PostMapping
    public ResponseDto create(@RequestBody RequestDto request) {
        return service.create(request);
    }

    /**
     * Met à jour une ressource existante à partir de son identifiant.
     *
     * @param id l'identifiant unique de la ressource à modifier
     * @param request l'objet {@link RequestDto} contenant les nouvelles données
     * @return un objet {@link ResponseDto} représentant la ressource mise à jour
     */
    @PutMapping("/{id}")
    public ResponseDto update(@PathVariable Long id, @RequestBody RequestDto request) {
        return service.update(id, request);
    }

    /**
     * Supprime une ressource spécifique à partir de son identifiant.
     *
     * @param id l'identifiant unique de la ressource à supprimer
     * @return un objet {@link ResponseDto} représentant la ressource qui vient d'être supprimée
     * @throws Throwable si la ressource à supprimer n'existe pas
     */
    @DeleteMapping("/{id}")
    public ResponseDto delete(@PathVariable Long id) throws Throwable {
        return service.delete(id);
    }
}
