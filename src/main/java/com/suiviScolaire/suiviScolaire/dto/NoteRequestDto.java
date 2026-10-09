package com.suiviScolaire.suiviScolaire.dto;

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
public class NoteRequestDto implements RequestDto {
    private Long id;
    private BigDecimal valeur;
    private Long inscriptionId;
    private Long enseignementId;
    private Long periodeId;
    private Long typeEvaluationId;
}