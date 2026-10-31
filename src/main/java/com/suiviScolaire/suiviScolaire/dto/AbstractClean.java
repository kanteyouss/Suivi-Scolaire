package com.suiviScolaire.suiviScolaire.dto;

/**
 * Classe abstraite de base pour le nettoyage des chaînes de caractères.
 * Permet de supprimer les espaces superflus (début, fin, espaces multiples)
 * afin d'éviter les doublons et incohérences de données.
 */
public abstract class AbstractClean {

    protected String cleanText(String text) {
        return text != null ? text.replaceAll("\\s+", " ").trim() : null;
    }
}
