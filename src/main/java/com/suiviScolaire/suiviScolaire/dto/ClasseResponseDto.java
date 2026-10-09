package com.suiviScolaire.suiviScolaire.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * Réponse contenant les données d'une classe.
 * Le champ effectifReel n'est renseigné que pour le détail d'une classe.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClasseResponseDto implements ResponseDto {
    private Long id;
    private String nom;
    private String niveau;
    private Integer effectifMax;
    private Long effectifReel;
}