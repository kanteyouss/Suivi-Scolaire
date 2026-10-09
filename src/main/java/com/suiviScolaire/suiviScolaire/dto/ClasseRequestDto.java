package com.suiviScolaire.suiviScolaire.dto;

import lombok.*;

/**
 * Requête de création ou de modification d'une classe.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClasseRequestDto implements RequestDto {
    private Long id;
    private String nom;
    private String niveau;
    private Integer effectifMax;
}