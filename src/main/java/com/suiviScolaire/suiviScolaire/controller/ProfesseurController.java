package com.suiviScolaire.suiviScolaire.controller;

import com.suiviScolaire.suiviScolaire.dto.ProfesseurRequestDto;
import com.suiviScolaire.suiviScolaire.dto.ResponseDto;
import com.suiviScolaire.suiviScolaire.service.ProfesseurService;
import com.suiviScolaire.suiviScolaire.utils.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour la gestion des professeurs et de leurs affectations pédagogiques.
 * Délègue l'ensemble des règles métier et de persistance au service {@link ProfesseurService}.
 */
@RestController
@RequestMapping("/professeurs")
@Tag(name = "Professeurs", description = "Endpoints pour la gestion des professeurs et de leurs affectations pédagogiques")
public class ProfesseurController {

    private final ProfesseurService professeurService;

    /**
     * Constructeur injectant le service de gestion des professeurs.
     *
     * @param professeurService le service métier des professeurs
     */
    public ProfesseurController(ProfesseurService professeurService) {
        this.professeurService = professeurService;
    }

    /**
     * Récupère la liste paginée de tous les professeurs enregistrés.
     *
     * @param page Numéro de la page (commence à 0).
     * @param taille Nombre d'éléments par page.
     * @return Réponse contenant la liste des professeurs.
     */
    @Operation(summary = "Lister tous les professeurs", description = "Récupère la liste paginée de tous les professeurs enregistrés.")
    @GetMapping
    public Response<ResponseDto> getAll(
            @Parameter(description = "Numéro de la page (commence à 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Nombre d'éléments par page") @RequestParam(defaultValue = "10") int taille) {
        return new Response<>(professeurService.getAll(page, taille).getContent());
    }

    /**
     * Récupère les détails d'un professeur spécifique à partir de son identifiant.
     *
     * @param id Identifiant unique du professeur.
     * @return Détails du professeur trouvé.
     * @throws Throwable en cas d'erreur ou d'introuvabilité
     */
    @Operation(summary = "Obtenir un professeur par ID", description = "Retourne les détails d'un professeur spécifique à partir de son identifiant.")
    @GetMapping("/{id}")
    public Response<ResponseDto> getById(
            @Parameter(description = "ID unique du professeur", required = true) @PathVariable Long id) throws Throwable {
        return new Response<>(professeurService.getById(id));
    }

    /**
     * Recherche un professeur par son nom ou son prénom.
     *
     * @param nom Nom ou prénom recherché.
     * @return Détails du professeur trouvé.
     * @throws Throwable en cas d'erreur ou d'introuvabilité
     */
    @Operation(summary = "Rechercher un professeur par nom", description = "Permet de rechercher un professeur en fonction de son nom ou prénom.")
    @GetMapping("/recherche")
    public Response<ResponseDto> getByNom(
            @Parameter(description = "Nom ou partie du nom du professeur", required = true) @RequestParam String nom) throws Throwable {
        return new Response<>(professeurService.getByNom(nom));
    }

    /**
     * Enregistre un nouveau professeur dans le système.
     *
     * @param professeurRequest Données du professeur à créer.
     * @return Professeur créé.
     */
    @Operation(summary = "Créer un nouveau professeur", description = "Enregistre un nouveau professeur dans le système.")
    @PostMapping
    public Response<ResponseDto> create(@Valid @RequestBody ProfesseurRequestDto professeurRequest) {
        return new Response<>(professeurService.create(professeurRequest));
    }

    /**
     * Met à jour les informations d'un professeur existant.
     *
     * @param id Identifiant du professeur à modifier.
     * @param professeurRequest Nouvelles données du professeur.
     * @return Professeur mis à jour.
     */
    @Operation(summary = "Mettre à jour un professeur", description = "Modifie les informations d'un professeur existant.")
    @PutMapping("/{id}")
    public Response<ResponseDto> update(
            @Parameter(description = "ID du professeur à modifier", required = true) @PathVariable Long id,
            @Valid @RequestBody ProfesseurRequestDto professeurRequest) {
        return new Response<>(professeurService.update(id, professeurRequest));
    }

    /**
     * Supprime un professeur à partir de son identifiant.
     *
     * @param id Identifiant du professeur à supprimer.
     * @return Confirmation de suppression.
     * @throws Throwable en cas d'erreur
     */
    @Operation(summary = "Supprimer un professeur", description = "Supprime un professeur à partir de son ID.")
    @DeleteMapping("/{id}")
    public Response<ResponseDto> delete(
            @Parameter(description = "ID du professeur à supprimer", required = true) @PathVariable Long id) throws Throwable {
        return new Response<>(professeurService.delete(id));
    }

    /**
     * Associe un professeur à une matière spécifique dans une classe donnée (enseignement).
     *
     * @param id Identifiant unique du professeur.
     * @param matiereId Identifiant de la matière à enseigner.
     * @param classeId Identifiant de la classe concernée.
     * @return Confirmation de l'affectation.
     */
    @Operation(summary = "Affecter une matière et une classe à un professeur", description = "Associe un professeur à une matière spécifique dans une classe donnée.")
    @PostMapping("/{id}/affectations")
    public Response<ResponseDto> affecterMatiereAProfesseurDansClasse(
            @Parameter(description = "ID unique du professeur", required = true) @PathVariable Long id,
            @Parameter(description = "ID unique de la matière", required = true) @RequestParam Long matiereId,
            @Parameter(description = "ID unique de la classe", required = true) @RequestParam Long classeId) {
        return new Response<>(professeurService.affecterMatiereAProfesseurDansClasse(id, matiereId, classeId));
    }
}