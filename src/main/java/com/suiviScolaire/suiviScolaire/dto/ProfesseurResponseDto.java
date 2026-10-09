package com.suiviScolaire.suiviScolaire.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

/**
 * Réponse contenant les données d'un professeur.
 * Le champ matieres n'est renseigné que pour le détail d'un professeur.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfesseurResponseDto implements ResponseDto {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private List<MatiereResponseDto> matieres;
}