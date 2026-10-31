package com.suiviScolaire.suiviScolaire.controller;

import com.suiviScolaire.suiviScolaire.dto.EleveRequestDto;
import com.suiviScolaire.suiviScolaire.dto.MoyenneGeneraleResponseDto;
import com.suiviScolaire.suiviScolaire.dto.MoyennesParMatiereResponseDto;
import com.suiviScolaire.suiviScolaire.dto.NotesParMatiereResponseDto;
import com.suiviScolaire.suiviScolaire.dto.ResponseDto;
import com.suiviScolaire.suiviScolaire.service.EleveService;
import com.suiviScolaire.suiviScolaire.service.NoteService;
import com.suiviScolaire.suiviScolaire.utils.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour la gestion des élèves et de leurs parcours scolaires.
 * Délègue l'ensemble des règles métier aux services {@link EleveService} et {@link NoteService}.
 */
@RestController
@RequestMapping("/eleves")
@Tag(name = "Élèves", description = "Endpoints pour la gestion des élèves, des inscriptions, des notes et des moyennes")
public class EleveController {

    private final EleveService eleveService;
    private final NoteService noteService;

    /**
     * Constructeur injectant les services nécessaires.
     *
     * @param eleveService le service métier des élèves
     * @param noteService le service métier des notes
     */
    public EleveController(EleveService eleveService, NoteService noteService) {
        this.eleveService = eleveService;
        this.noteService = noteService;
    }

    /**
     * Récupère la liste paginée des élèves ou effectue une recherche filtrée par nom.
     *
     * @param page Numéro de la page (commence à 0).
     * @param taille Nombre d'éléments par page.
     * @param nom Nom ou fragment de nom de l'élève à rechercher (optionnel).
     * @return Réponse contenant la liste des élèves.
     */
    @Operation(summary = "Lister tous les élèves", description = "Récupère la liste paginée des élèves avec la possibilité d'effectuer un filtrage par nom.")
    @GetMapping
    public Response<ResponseDto> getAll(
            @Parameter(description = "Numéro de la page (commence à 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Nombre d'éléments par page") @RequestParam(defaultValue = "10") int taille,
            @Parameter(description = "Nom de l'élève à rechercher (optionnel)") @RequestParam(required = false) String nom) {
        return eleveService.getAllOrSearch(page, taille, nom);
    }

    /**
     * Récupère les notes d'un élève regroupées par matière, avec filtrage optionnel par trimestre.
     *
     * @param id Identifiant unique de l'élève.
     * @param trimestre Numéro du trimestre (1, 2 ou 3, optionnel).
     * @return Les notes de l'élève groupées par matière.
     */
    @Operation(summary = "Obtenir les notes d'un élève", description = "Récupère les notes d'un élève regroupées par matière, avec filtrage optionnel par trimestre.")
    @GetMapping("/{id}/notes")
    public Response<NotesParMatiereResponseDto> getNotesByEleve(
            @Parameter(description = "ID unique de l'élève", required = true) @PathVariable Long id,
            @Parameter(description = "Numéro du trimestre (1, 2 ou 3)") @RequestParam(required = false) Integer trimestre) {
        return new Response<>(noteService.getNotesByEleve(id, trimestre));
    }

    /**
     * Récupère un élève à partir de son identifiant.
     *
     * @param id Identifiant de l'élève.
     * @return Les détails de l'élève.
     * @throws Throwable en cas d'erreur
     */
    @Operation(summary = "Obtenir un élève par ID", description = "Retourne les détails d'un élève spécifique à partir de son identifiant.")
    @GetMapping("/{id}")
    public Response<ResponseDto> getById(
            @Parameter(description = "ID unique de l'élève", required = true) @PathVariable Long id) throws Throwable {
        return new Response<>(eleveService.getById(id));
    }

    /**
     * Recherche un élève par son nom.
     *
     * @param nom Nom ou partie du nom de l'élève.
     * @return Détails de l'élève trouvé.
     * @throws Throwable en cas d'erreur
     */
    @Operation(summary = "Rechercher un élève par nom", description = "Permet de rechercher un élève en fonction de son nom ou prénom.")
    @GetMapping("/recherche")
    public Response<ResponseDto> getByNom(
            @Parameter(description = "Nom ou partie du nom de l'élève", required = true) @RequestParam String nom) throws Throwable {
        return new Response<>(eleveService.getByNom(nom));
    }

    /**
     * Enregistre un nouvel élève.
     *
     * @param eleveRequest Informations de l'élève à créer.
     * @return Élève créé.
     */
    @Operation(summary = "Créer un nouvel élève", description = "Enregistre un nouvel élève dans le système.")
    @PostMapping
    public Response<ResponseDto> create(@Valid @RequestBody EleveRequestDto eleveRequest) {
        return new Response<>(eleveService.create(eleveRequest));
    }

    /**
     * Met à jour les informations d'un élève existant.
     *
     * @param id Identifiant de l'élève à modifier.
     * @param eleveRequest Nouvelles données de l'élève.
     * @return Élève mis à jour.
     */
    @Operation(summary = "Mettre à jour un élève", description = "Modifie les informations d'un élève existant.")
    @PutMapping("/{id}")
    public Response<ResponseDto> update(
            @Parameter(description = "ID de l'élève à modifier", required = true) @PathVariable Long id,
            @Valid @RequestBody EleveRequestDto eleveRequest) {
        return new Response<>(eleveService.update(id, eleveRequest));
    }

    /**
     * Affecte un élève à une classe pour une année scolaire.
     *
     * @param eleveId Identifiant de l'élève.
     * @param nouvelleClasseId Identifiant de la classe de destination.
     * @param anneeId Identifiant de l'année scolaire (optionnel).
     * @return Confirmation de l'affectation.
     */
    @Operation(summary = "Affecter un élève à une classe", description = "Affecte un élève à une nouvelle classe pour une année scolaire donnée.")
    @PostMapping("/{eleveId}/affectation")
    public Response<ResponseDto> affecterEleveClasse(
            @Parameter(description = "ID unique de l'élève", required = true) @PathVariable Long eleveId,
            @Parameter(description = "ID de la nouvelle classe de destination") @RequestParam(required = false) Long nouvelleClasseId,
            @Parameter(description = "ID de l'année scolaire") @RequestParam(required = false) Long anneeId) {
        return new Response<>(eleveService.affecterEleveClasse(eleveId, nouvelleClasseId, anneeId));
    }

    /**
     * Supprime un élève à partir de son identifiant.
     *
     * @param id Identifiant de l'élève à supprimer.
     * @return Confirmation de la suppression.
     * @throws Throwable en cas d'erreur
     */
    @Operation(summary = "Supprimer un élève", description = "Supprime un élève à partir de son ID.")
    @DeleteMapping("/{id}")
    public Response<ResponseDto> delete(
            @Parameter(description = "ID de l'élève à supprimer", required = true) @PathVariable Long id) throws Throwable {
        return new Response<>(eleveService.delete(id));
    }

    /**
     * Calcule et retourne les moyennes par matière d'un élève pour un trimestre donné.
     *
     * @param id Identifiant unique de l'élève.
     * @param trimestre Numéro du trimestre (1, 2 ou 3).
     * @return Moyennes par matière.
     */
    @Operation(summary = "Obtenir les moyennes par matière d'un élève", description = "Calcule et retourne les moyennes de l'élève pour chaque matière sur un trimestre donné.")
    @GetMapping("/{id}/moyennes")
    public Response<MoyennesParMatiereResponseDto> getMoyennesParMatiere(
            @Parameter(description = "ID unique de l'élève", required = true) @PathVariable Long id,
            @Parameter(description = "Numéro du trimestre (1, 2 ou 3)", example = "1", required = true) @RequestParam Integer trimestre) {
        return new Response<>(noteService.getMoyennesParMatiere(id, trimestre));
    }

    /**
     * Calcule et retourne la moyenne générale pondérée d'un élève pour un trimestre donné.
     *
     * @param id Identifiant unique de l'élève.
     * @param trimestre Numéro du trimestre (1, 2 ou 3).
     * @return Moyenne générale et détails par matière.
     */
    @Operation(summary = "Obtenir la moyenne générale d'un élève", description = "Calcule et retourne la moyenne générale de l'élève pour un trimestre donné.")
    @GetMapping("/{id}/moyenne-generale")
    public Response<MoyenneGeneraleResponseDto> getMoyenneGenerale(
            @Parameter(description = "ID unique de l'élève", required = true) @PathVariable Long id,
            @Parameter(description = "Numéro du trimestre (1, 2 ou 3)", example = "1", required = true) @RequestParam Integer trimestre) {
        return new Response<>(noteService.getMoyenneGenerale(id, trimestre));
    }
}
