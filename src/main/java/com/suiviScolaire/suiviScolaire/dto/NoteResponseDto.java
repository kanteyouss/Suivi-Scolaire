package com.suiviScolaire.suiviScolaire.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Réponse contenant les données d'une note, avec l'inscription, l'enseignement,
 * la période et le type d'évaluation associés.
 * Les champs eleve et matiere ne sont renseignés que pour les vues détaillées.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Représentation détaillée d'une note scolaire")
public class NoteResponseDto implements ResponseDto {

    @Schema(description = "Identifiant unique de la note", example = "1")
    private Long id;

    @Schema(description = "Valeur numérique de la note sur 20", example = "15.5")
    private BigDecimal valeur;

    @Schema(description = "Date d'enregistrement de la note au format ISO (AAAA-MM-JJ)", example = "2026-10-30", format = "date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateSaisie;

    @Schema(description = "Détails de l'inscription scolaire associée")
    private InscriptionResponseDto inscription;

    @Schema(description = "Détails de l'enseignement concerné")
    private EnseignementResponseDto enseignement;

    @Schema(description = "Période / Trimestre d'évaluation")
    private PeriodeResponseDto periode;

    @Schema(description = "Type d'évaluation (devoir, interrogation, etc.)")
    private TypeEvaluationResponseDto typeEvaluation;

    @Schema(description = "Détails de l'élève concerné (si vue détaillée)")
    private EleveResponseDto eleve;

    @Schema(description = "Détails de la matière concernée (si vue détaillée)")
    private MatiereResponseDto matiere;
}
