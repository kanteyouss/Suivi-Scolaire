package com.suiviScolaire.suiviScolaire.dto;

import lombok.*;

/**
 * Requête de création ou de modification d'une matière.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MatiereRequestDto implements RequestDto {
    private Long id;
    private String nom;
}