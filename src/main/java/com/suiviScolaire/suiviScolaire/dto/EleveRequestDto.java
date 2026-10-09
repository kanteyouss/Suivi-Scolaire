package com.suiviScolaire.suiviScolaire.dto;

import lombok.*;

import java.time.LocalDate;

/**
 * Requête de création ou de modification d'un élève.
 * La classe de rattachement (et l'année) sont optionnelles à la création :
 * elles servent à créer l'inscription correspondante.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EleveRequestDto implements RequestDto {
    private Long id;
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private Long classeId;
    private Long anneeId;
}