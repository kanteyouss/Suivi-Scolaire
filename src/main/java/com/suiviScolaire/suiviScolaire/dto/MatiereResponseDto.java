package com.suiviScolaire.suiviScolaire.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

/**
 * Réponse contenant les données d'une matière.
 * Le champ coefficients n'est renseigné que pour le détail d'une matière.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MatiereResponseDto implements ResponseDto {
    private Long id;
    private String nom;
    private List<CoefficientResponseDto> coefficients;
}