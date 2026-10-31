package com.suiviScolaire.suiviScolaire.controller;

import com.suiviScolaire.suiviScolaire.dto.ResponseDto;
import com.suiviScolaire.suiviScolaire.service.TypeEvaluationService;
import com.suiviScolaire.suiviScolaire.utils.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Contrôleur REST pour la configuration des types d'évaluation et de leurs pondérations.
 * Délègue l'ensemble des règles métier au service {@link TypeEvaluationService}.
 */
@RestController
@RequestMapping("/types-evaluation")
@Tag(name = "Types d'évaluation", description = "Endpoints pour la gestion des types d'évaluation et la configuration de leurs poids/coefficients")
public class TypeEvaluationController {

    private final TypeEvaluationService typeEvaluationService;

    /**
     * Constructeur injectant le service de gestion des types d'évaluation.
     *
     * @param typeEvaluationService le service métier des types d'évaluation
     */
    public TypeEvaluationController(TypeEvaluationService typeEvaluationService) {
        this.typeEvaluationService = typeEvaluationService;
    }

    /**
     * Modifie le coefficient ou le poids appliqué à un type d'évaluation (ex: devoir, interrogation).
     *
     * @param code Code unique du type d'évaluation.
     * @param poids Nouveau poids / coefficient strictly positif.
     * @return Confirmation de la modification du poids.
     */
    @Operation(summary = "Modifier le poids d'un type d'évaluation", description = "Met à jour le coefficient ou le poids appliqué à un type d'évaluation (ex: devoir, interrogation, oral).")
    @PutMapping("/{code}/poids")
    public Response<ResponseDto> modifierPoids(
            @Parameter(description = "Code unique du type d'évaluation (ex: 'devoir', 'interrogation', 'oral')", example = "devoir", required = true)
            @PathVariable String code,
            @Parameter(description = "Nouveau poids/coefficient (doit être strictement positif)", example = "2.0", required = true)
            @RequestParam BigDecimal poids) {
        return new Response<>(typeEvaluationService.modifierPoids(code, poids));
    }
}