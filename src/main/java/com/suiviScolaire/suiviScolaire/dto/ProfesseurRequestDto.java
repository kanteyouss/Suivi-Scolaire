package com.suiviScolaire.suiviScolaire.dto;

import lombok.*;

/**
 * Requête de création ou de modification d'un professeur.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfesseurRequestDto implements RequestDto {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
}