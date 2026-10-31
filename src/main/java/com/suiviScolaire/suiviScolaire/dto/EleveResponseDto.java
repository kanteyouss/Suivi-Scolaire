package com.suiviScolaire.suiviScolaire.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Représentation détaillée d'un élève")
public class EleveResponseDto implements ResponseDto {

    @Schema(description = "Identifiant unique de l'élève", example = "1")
    private Long id;

    @Schema(description = "Numéro de matricule unique de l'élève", example = "ELV-1")
    private String matricule;

    @Schema(description = "Nom de famille de l'élève", example = "Kouassi")
    private String nom;

    @Schema(description = "Prénom(s) de l'élève", example = "Jean-Marc")
    private String prenom;

    @Schema(description = "Date de naissance de l'élève au format ISO (AAAA-MM-JJ)", example = "2013-05-10", format = "date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateNaissance;

    @Schema(description = "Classe actuelle de l'élève")
    private ClasseResponseDto classe;
}
