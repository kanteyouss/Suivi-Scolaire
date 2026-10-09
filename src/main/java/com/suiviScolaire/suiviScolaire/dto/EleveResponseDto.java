package com.suiviScolaire.suiviScolaire.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;

/**
 * Réponse contenant les données d'un élève.
 * Le champ classe n'est renseigné que pour le détail d'un élève.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EleveResponseDto implements ResponseDto {
    private Long id;
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private ClasseResponseDto classe;
}