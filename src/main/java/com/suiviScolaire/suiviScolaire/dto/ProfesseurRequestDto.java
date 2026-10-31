package com.suiviScolaire.suiviScolaire.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Requête de création ou de modification d'un professeur.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfesseurRequestDto extends AbstractClean implements RequestDto {
    private Long id;
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;
    @NotBlank(message = "Le prenom est obligatoire")
    private String prenom;
    @NotBlank(message = "L email est obligatoire")
    @Email(message = "L email doit être valide")
    private String email;
    @NotNull(message = "La matière principale est obligatoire")
    @Positive(message = "L identifiant de la matière doit être positif")
    private Long matiereId;

    @JsonSetter("nom")
    public void setNom(String nom) {
        this.nom = cleanText(nom);
    }

    @JsonSetter("prenom")
    public void setPrenom(String prenom) {
        this.prenom = cleanText(prenom);
    }

    @JsonSetter("email")
    public void setEmail(String email) {
        this.email = cleanText(email);
    }
}