package com.suiviScolaire.suiviScolaire.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/**
 * Requête de création ou de modification d'une note.
 * L'élève, la matière, la période et le type d'évaluation sont transmis via leurs identifiants.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoteRequestDto extends AbstractClean implements RequestDto {
    private Long id;
    @NotNull(message = "La valeur est obligatoire")
    @DecimalMin(value = "0", message = "La valeur doit etre positive")
    @DecimalMax(value = "20", message = "La valeur doit etre comprise entre 0 et 20")
    @Digits(integer = 3, fraction = 2, message = "Valeur invalide: 3 chiffres max, 2 decimales")
    private BigDecimal valeur;
    @NotNull(message = "L inscription est obligatoire")
    private Long inscriptionId;
    @NotNull(message = "L enseignement est obligatoire")
    private Long enseignementId;
    @NotNull(message = "La periode est obligatoire")
    private Long periodeId;
    @NotNull(message = "Le type d evaluation est obligatoire")
    private Long typeEvaluationId;
}