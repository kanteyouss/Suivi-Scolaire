package com.suiviScolaire.suiviScolaire.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Requête de création ou de modification d'une matière.
 * Le type polymorphe est désactivé ici : les champs {id, nom} de cette DTO
 * sont un sous-ensemble strict de ceux des autres RequestDto (Classe, Eleve,
 * Professeur), ce qui rendrait la déduction Jackson (DEDUCTION) ambiguë et
 * ferait échouer toute désérialisation. Le contrôleur lie directement ce
 * type concret, la déduction n'est donc pas nécessaire.
 */
@Getter
@Setter
@Builder
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@AllArgsConstructor
@NoArgsConstructor
public class MatiereRequestDto extends AbstractClean implements RequestDto {
    private Long id;
    @NotBlank(message = "Le libelle est obligatoire")
    private String libelle;
    @NotNull(message = "Le coefficient est obligatoire")
    @DecimalMin(value = "0", inclusive = false, message = "Le coefficient doit être strictement positif")
    private BigDecimal coefficient;

    @JsonSetter("libelle")
    public void setLibelle(String libelle) {
        this.libelle = cleanText(libelle);
    }
}