package com.suiviScolaire.suiviScolaire.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Requête de création ou de modification d'une classe.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClasseRequestDto extends AbstractClean implements RequestDto {
    private Long id;
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;
    @NotBlank(message = "Le niveau est obligatoire")
    private String niveau;
    @NotNull(message = "L effectif max est obligatoire")
    @Positive(message = "L effectif max doit être positif")
    private Integer effectifMax;

    @JsonSetter("nom")
    public void setNom(String nom) {
        this.nom = cleanText(nom);
    }

    @JsonSetter("niveau")
    public void setNiveau(String niveau) {
        this.niveau = cleanText(niveau);
    }
}