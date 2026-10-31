package com.suiviScolaire.suiviScolaire.controller;

import com.suiviScolaire.suiviScolaire.dto.MatiereRequestDto;
import com.suiviScolaire.suiviScolaire.dto.ResponseDto;
import com.suiviScolaire.suiviScolaire.service.MatiereService;
import com.suiviScolaire.suiviScolaire.utils.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour la gestion du référentiel des matières scolaires.
 * Délègue l'ensemble des règles métier et de persistance au service {@link MatiereService}.
 */
@RestController
@RequestMapping("/matieres")
@Tag(name = "Matières", description = "Endpoints pour la gestion du référentiel des matières enseignées")
public class MatiereController {

    private final MatiereService matiereService;

    /**
     * Constructeur injectant le service de gestion des matières.
     *
     * @param matiereService le service métier des matières
     */
    public MatiereController(MatiereService matiereService) {
        this.matiereService = matiereService;
    }

    /**
     * Récupère la liste paginée de toutes les matières enregistrées.
     *
     * @param page Numéro de la page (commence à 0).
     * @param taille Nombre d'éléments par page.
     * @return Réponse contenant la liste des matières.
     */
    @Operation(summary = "Lister toutes les matières", description = "Récupère la liste paginée de toutes les matières enregistrées.")
    @GetMapping
    public Response<ResponseDto> getAll(
            @Parameter(description = "Numéro de la page (commence à 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Nombre d'éléments par page") @RequestParam(defaultValue = "10") int taille) {
        return new Response<>(matiereService.getAll(page, taille).getContent());
    }

    /**
     * Récupère les détails d'une matière spécifique à partir de son identifiant.
     *
     * @param id Identifiant unique de la matière.
     * @return Détails de la matière trouvée.
     * @throws Throwable en cas d'erreur ou d'introuvabilité
     */
    @Operation(summary = "Obtenir une matière par ID", description = "Retourne les détails d'une matière spécifique à partir de son identifiant.")
    @GetMapping("/{id}")
    public Response<ResponseDto> getById(
            @Parameter(description = "ID unique de la matière", required = true) @PathVariable Long id) throws Throwable {
        return new Response<>(matiereService.getById(id));
    }

    /**
     * Recherche une matière par son nom ou son libellé.
     *
     * @param nom Nom ou partie du nom de la matière.
     * @return Détails de la matière trouvée.
     * @throws Throwable en cas d'erreur ou d'introuvabilité
     */
    @Operation(summary = "Rechercher une matière par nom", description = "Permet de rechercher une matière en fonction de son nom ou intitule.")
    @GetMapping("/recherche")
    public Response<ResponseDto> getByNom(
            @Parameter(description = "Nom ou partie du nom de la matière", required = true) @RequestParam String nom) throws Throwable {
        return new Response<>(matiereService.getByNom(nom));
    }

    /**
     * Enregistre une nouvelle matière dans le système.
     *
     * @param matiereRequest Données de la matière à créer.
     * @return Matière créée.
     */
    @Operation(summary = "Créer une nouvelle matière", description = "Enregistre une nouvelle matière dans le système.")
    @PostMapping
    public Response<ResponseDto> create(@Valid @RequestBody MatiereRequestDto matiereRequest) {
        return new Response<>(matiereService.create(matiereRequest));
    }

    /**
     * Met à jour les informations d'une matière existante.
     *
     * @param id Identifiant de la matière à modifier.
     * @param matiereRequest Nouvelles données de la matière.
     * @return Matière mise à jour.
     */
    @Operation(summary = "Mettre à jour une matière", description = "Modifie les informations d'une matière existante.")
    @PutMapping("/{id}")
    public Response<ResponseDto> update(
            @Parameter(description = "ID de la matière à modifier", required = true) @PathVariable Long id,
            @Valid @RequestBody MatiereRequestDto matiereRequest) {
        return new Response<>(matiereService.update(id, matiereRequest));
    }

    /**
     * Supprime une matière à partir de son identifiant.
     *
     * @param id Identifiant de la matière à supprimer.
     * @return Confirmation de suppression.
     * @throws Throwable en cas d'erreur
     */
    @Operation(summary = "Supprimer une matière", description = "Supprime une matière à partir de son ID.")
    @DeleteMapping("/{id}")
    public Response<ResponseDto> delete(
            @Parameter(description = "ID de la matière à supprimer", required = true) @PathVariable Long id) throws Throwable {
        return new Response<>(matiereService.delete(id));
    }
}