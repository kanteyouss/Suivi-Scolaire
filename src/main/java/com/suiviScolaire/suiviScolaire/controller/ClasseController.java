package com.suiviScolaire.suiviScolaire.controller;

import com.suiviScolaire.suiviScolaire.dto.ClasseElevesResponseDto;
import com.suiviScolaire.suiviScolaire.dto.ClasseNotesResponseDto;
import com.suiviScolaire.suiviScolaire.dto.ClasseRequestDto;
import com.suiviScolaire.suiviScolaire.dto.ClassementResponseDto;
import com.suiviScolaire.suiviScolaire.dto.ResponseDto;
import com.suiviScolaire.suiviScolaire.service.ClasseService;
import com.suiviScolaire.suiviScolaire.service.EleveService;
import com.suiviScolaire.suiviScolaire.service.NoteService;
import com.suiviScolaire.suiviScolaire.utils.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour la gestion des classes scolaires.
 * Délègue les opérations métier aux services {@link ClasseService}, {@link EleveService} et {@link NoteService}.
 */
@RestController
@RequestMapping("/classes")
@Tag(name = "Classes", description = "Endpoints pour la gestion des classes, des effectifs, des notes et des classements scolaires")
public class ClasseController {

    private final ClasseService classeService;
    private final EleveService eleveService;
    private final NoteService noteService;

    /**
     * Constructeur injectant les services nécessaires.
     *
     * @param classeService le service de gestion des classes
     * @param eleveService le service de gestion des élèves
     * @param noteService le service de gestion des notes
     */
    public ClasseController(ClasseService classeService, EleveService eleveService, NoteService noteService) {
        this.classeService = classeService;
        this.eleveService = eleveService;
        this.noteService = noteService;
    }

    /**
     * Récupère la liste paginée des classes.
     *
     * @param page Numéro de la page (commence à 0).
     * @param taille Nombre d'éléments par page.
     * @return Réponse paginée contenant les classes.
     */
    @Operation(summary = "Lister toutes les classes", description = "Récupère la liste paginée de toutes les classes enregistrées.")
    @GetMapping
    public Response<ResponseDto> getAll(
            @Parameter(description = "Numéro de la page (commence à 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Nombre d'éléments par page") @RequestParam(defaultValue = "10") int taille) {
        return new Response<>(classeService.getAll(page, taille).getContent(), classeService.getAll(page, taille).getTotalElements(), classeService.getAll(page, taille).getTotalPages(), page, taille);
    }

    /**
     * Récupère les élèves inscrits dans une classe.
     *
     * @param id Identifiant unique de la classe.
     * @return Liste et effectif des élèves de la classe.
     */
    @Operation(summary = "Obtenir les élèves d'une classe", description = "Récupère l'effectif et la liste des élèves d'une classe triés par nom puis prénom.")
    @GetMapping("/{id}/eleves")
    public Response<ClasseElevesResponseDto> getElevesByClasse(
            @Parameter(description = "ID unique de la classe", required = true) @PathVariable Long id) {
        return new Response<>(eleveService.getElevesByClasse(id));
    }

    /**
     * Récupère les notes des élèves d'une classe pour une matière et un trimestre donnés.
     *
     * @param id Identifiant unique de la classe.
     * @param matiere Identifiant de la matière.
     * @param trimestre Numéro du trimestre (1, 2 ou 3).
     * @return Les notes de la classe pour la matière et le trimestre.
     */
    @Operation(summary = "Obtenir les notes d'une classe par matière et trimestre", description = "Récupère les notes obtenues par les élèves d'une classe pour une matière et un trimestre donnés.")
    @GetMapping("/{id}/notes")
    public Response<ClasseNotesResponseDto> getNotesByClasse(
            @Parameter(description = "ID unique de la classe", required = true) @PathVariable Long id,
            @Parameter(description = "ID unique de la matière", required = true) @RequestParam Long matiere,
            @Parameter(description = "Numéro du trimestre (1, 2 ou 3)", example = "1", required = true) @RequestParam Integer trimestre) {
        return new Response<>(noteService.getNotesByClasse(id, matiere, trimestre));
    }

    /**
     * Récupère les détails d'une classe par son identifiant.
     *
     * @param id Identifiant de la classe.
     * @return Détails de la classe.
     * @throws Throwable en cas d'erreur
     */
    @Operation(summary = "Obtenir une classe par ID", description = "Retourne les détails d'une classe spécifique à partir de son identifiant.")
    @GetMapping("/{id}")
    public Response<ResponseDto> getById(
            @Parameter(description = "ID unique de la classe", required = true) @PathVariable Long id) throws Throwable {
        return new Response<>(classeService.getById(id));
    }

    /**
     * Recherche une classe par son nom.
     *
     * @param nom Nom ou partie du nom de la classe.
     * @return Classe trouvée.
     * @throws Throwable en cas d'erreur
     */
    @Operation(summary = "Rechercher une classe par nom", description = "Permet de rechercher une classe en fonction de son nom ou libellé.")
    @GetMapping("/recherche")
    public Response<ResponseDto> getByNom(
            @Parameter(description = "Nom ou partie du nom de la classe", required = true) @RequestParam String nom) throws Throwable {
        return new Response<>(classeService.getByNom(nom));
    }

    /**
     * Crée une nouvelle classe.
     *
     * @param classeRequest Données de création de la classe.
     * @return Classe créée.
     */
    @Operation(summary = "Créer une nouvelle classe", description = "Enregistre une nouvelle classe dans le système.")
    @PostMapping
    public Response<ResponseDto> create(@Valid @RequestBody ClasseRequestDto classeRequest) {
        return new Response<>(classeService.create(classeRequest));
    }

    /**
     * Met à jour une classe existante.
     *
     * @param id Identifiant de la classe à modifier.
     * @param classeRequest Nouvelles données de la classe.
     * @return Classe mise à jour.
     */
    @Operation(summary = "Mettre à jour une classe", description = "Modifie les informations d'une classe existante.")
    @PutMapping("/{id}")
    public Response<ResponseDto> update(
            @Parameter(description = "ID de la classe à modifier", required = true) @PathVariable Long id,
            @Valid @RequestBody ClasseRequestDto classeRequest) {
        return new Response<>(classeService.update(id, classeRequest));
    }

    /**
     * Supprime une classe.
     *
     * @param id Identifiant de la classe à supprimer.
     * @return Confirmation de suppression.
     * @throws Throwable en cas d'erreur
     */
    @Operation(summary = "Supprimer une classe", description = "Supprime une classe à partir de son ID.")
    @DeleteMapping("/{id}")
    public Response<ResponseDto> delete(
            @Parameter(description = "ID de la classe à supprimer", required = true) @PathVariable Long id) throws Throwable {
        return new Response<>(classeService.delete(id));
    }

    /**
     * Calcule et retourne le classement des élèves d'une classe pour un trimestre.
     *
     * @param id Identifiant unique de la classe.
     * @param trimestre Numéro du trimestre (1, 2 ou 3).
     * @return Classement des élèves.
     */
    @Operation(summary = "Obtenir le classement des élèves d'une classe", description = "Génère le classement général des élèves d'une classe pour un trimestre spécifique.")
    @GetMapping("/{id}/classement")
    public Response<ClassementResponseDto> getClassement(
            @Parameter(description = "ID unique de la classe", required = true) @PathVariable Long id,
            @Parameter(description = "Numéro du trimestre (1, 2 ou 3)", example = "1", required = true) @RequestParam Integer trimestre) {
        return new Response<>(noteService.getClassementClasse(id, trimestre));
    }
}
