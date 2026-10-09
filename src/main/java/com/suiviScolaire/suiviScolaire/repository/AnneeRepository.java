package com.suiviScolaire.suiviScolaire.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import com.suiviScolaire.suiviScolaire.entity.Annee;

/**
 * Repository JPA pour l'accès aux données des années scolaires.
 * Fournit les opérations CRUD standard sur la table "annee".
 */
@Repository
public interface AnneeRepository extends JpaRepository<Annee, Long> {

    /**
     * Récupère l'année scolaire la plus récente.
     *
     * @return Optional<Annee> L'année la plus récente si elle existe.
     */
    Optional<Annee> findTopByOrderByIdDesc();
}