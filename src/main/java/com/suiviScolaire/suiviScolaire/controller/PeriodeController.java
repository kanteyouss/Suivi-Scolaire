package com.suiviScolaire.suiviScolaire.controller;

import com.suiviScolaire.suiviScolaire.service.PeriodeService;
import com.suiviScolaire.suiviScolaire.utils.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour la gestion des périodes et trimestres scolaires.
 * Délègue l'ensemble des règles métier au service {@link PeriodeService}.
 */
@RestController
@RequestMapping("/trimestres")
@Tag(name = "Trimestres / Périodes", description = "Endpoints pour la consultation, la clôture et la réouverture exceptionnelle des trimestres scolaires")
public class PeriodeController {

    private final PeriodeService periodeService;

    /**
     * Constructeur injectant le service des périodes.
     *
     * @param periodeService le service métier des périodes
     */
    public PeriodeController(PeriodeService periodeService) {
        this.periodeService = periodeService;
    }

    /**
     * Rouvre un trimestre préalablement clôturé.
     *
     * @param numero Le numéro du trimestre (1, 2 ou 3).
     * @param motif Le motif explicite justifiant la réouverture (au moins 20 caractères).
     * @param auteur L'auteur de l'opération (par défaut: responsable_scolarite).
     * @return La réponse contenant le statut et message de réouverture.
     */
    @Operation(summary = "Rouvrir un trimestre clôturé", description = "Rouvre le dernier trimestre clôturé sous réserve de fournir un motif explicite d'au moins 20 caractères.")
    @PostMapping("/{numero}/reouverture")
    public ResponseEntity<Response<String>> reouvertureTrimestre(
            @Parameter(description = "Numéro du trimestre à rouvrir (1, 2 ou 3)", example = "1", required = true) @PathVariable Integer numero,
            @Parameter(description = "Motif justifiant la réouverture (minimum 20 caractères)", example = "Saisie tardive des notes d'évaluation de rattrapage en mathématiques", required = true) @RequestParam String motif,
            @Parameter(description = "Auteur de la réouverture (optionnel)") @RequestParam(defaultValue = "responsable_scolarite") String auteur) {
        return periodeService.reouvertureTrimestre(numero, motif, auteur);
    }

    /**
     * Clôture un trimestre scolaire après vérification des notes enregistrées.
     *
     * @param numero Le numéro du trimestre à clôturer (1, 2 ou 3).
     * @return La réponse confirmant la clôture ou indiquant les matières manquantes.
     */
    @Operation(summary = "Clôturer un trimestre", description = "Clôture un trimestre après vérification que toutes les matières de toutes les classes possèdent au moins une note enregistrée.")
    @PostMapping("/{numero}/cloture")
    public ResponseEntity<Response<String>> clotureTrimestre(
            @Parameter(description = "Numéro du trimestre à clôturer (1, 2 ou 3)", example = "1", required = true) @PathVariable Integer numero) {
        return periodeService.clotureTrimestre(numero);
    }
}
