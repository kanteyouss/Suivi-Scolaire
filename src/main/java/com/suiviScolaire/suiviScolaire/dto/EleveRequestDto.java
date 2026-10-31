package com.suiviScolaire.suiviScolaire.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSetter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;

/**
 * Requête de création ou de modification d'un élève.
 * La date de naissance doit être dans le passé et ne peut plus être modifiée après création.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Données pour la création ou la mise à jour d'un élève")
public class EleveRequestDto extends AbstractClean implements RequestDto {

    @Schema(description = "Identifiant unique de l'élève (généré automatiquement)", example = "1")
    private Long id;

    @Schema(description = "Numéro de matricule unique de l'élève", example = "ELV-1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Le matricule est obligatoire")
    private String matricule;

    @Schema(description = "Nom de famille de l'élève", example = "Kouassi", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @Schema(description = "Prénom(s) de l'élève", example = "Jean-Marc", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Le prenom est obligatoire")
    private String prenom;

    @Schema(description = "Date de naissance de l'élève au format ISO YYYY-MM-DD (AAAA-MM-JJ, ex: 2013-05-10). Doit obligatoirement être passée et ne pourra plus être modifiée après création.",
            example = "2013-05-10",
            format = "date",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit être dans le passé")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateNaissance;

    @Schema(description = "Identifiant de la classe de rattachement", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "La classe est obligatoire")
    @Positive(message = "L identifiant de la classe doit être positif")
    private Long classeId;

    @Schema(description = "Identifiant de l'année scolaire (optionnel, prend l'année courante par défaut)", example = "1")
    private Long anneeId;

    @JsonSetter("matricule")
    public void setMatricule(String matricule) {
        this.matricule = cleanText(matricule);
    }

    @JsonSetter("nom")
    public void setNom(String nom) {
        this.nom = cleanText(nom);
    }

    @JsonSetter("prenom")
    public void setPrenom(String prenom) {
        this.prenom = cleanText(prenom);
    }
}
