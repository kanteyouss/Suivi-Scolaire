package com.suiviScolaire.suiviScolaire.service;

import com.suiviScolaire.suiviScolaire.dto.PeriodeResponseDto;
import com.suiviScolaire.suiviScolaire.entity.Classe;
import com.suiviScolaire.suiviScolaire.entity.Matiere;
import com.suiviScolaire.suiviScolaire.entity.Periode;
import com.suiviScolaire.suiviScolaire.exception.MotifRouvreInvalideException;
import com.suiviScolaire.suiviScolaire.exception.NotFoundException;
import com.suiviScolaire.suiviScolaire.repository.ClasseRepository;
import com.suiviScolaire.suiviScolaire.repository.MatiereRepository;
import com.suiviScolaire.suiviScolaire.repository.NoteRepository;
import com.suiviScolaire.suiviScolaire.repository.PeriodeRepository;
import com.suiviScolaire.suiviScolaire.utils.response.Response;
import com.suiviScolaire.suiviScolaire.utils.response.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service métier gérant les périodes et trimestres scolaires.
 * Contient la logique d'ouverture, de clôture et de réouverture exceptionnelle des trimestres.
 */
@Service
public class PeriodeService {

    private final PeriodeRepository periodeRepository;
    private final ClasseRepository classeRepository;
    private final MatiereRepository matiereRepository;
    private final NoteRepository noteRepository;

    /**
     * Constructeur injectant les repositories nécessaires.
     *
     * @param periodeRepository le repository des périodes
     * @param classeRepository le repository des classes
     * @param matiereRepository le repository des matières
     * @param noteRepository le repository des notes
     */
    public PeriodeService(PeriodeRepository periodeRepository,
                          ClasseRepository classeRepository,
                          MatiereRepository matiereRepository,
                          NoteRepository noteRepository) {
        this.periodeRepository = periodeRepository;
        this.classeRepository = classeRepository;
        this.matiereRepository = matiereRepository;
        this.noteRepository = noteRepository;
    }

    /**
     * Convertit une entité période en objet de réponse DTO.
     *
     * @param periode L'entité période à convertir.
     * @return PeriodeResponseDto La réponse DTO de la période.
     */
    public PeriodeResponseDto toDto(Periode periode) {
        return PeriodeResponseDto.builder()
                .id(periode.getId())
                .nom(periode.getNom())
                .build();
    }

    /**
     * Rouvre un trimestre clôturé sous réserve d'un motif explicite d'au moins 20 caractères
     * et qu'aucun trimestre supérieur ne soit déjà clôturé.
     *
     * @param numero Numéro du trimestre (1, 2 ou 3).
     * @param motif Justificatif textuel (au moins 20 caractères).
     * @param auteur Identifiant de l'auteur de la réouverture.
     * @return Réponse HTTP avec statut de l'opération.
     */
    public ResponseEntity<Response<String>> reouvertureTrimestre(Integer numero, String motif, String auteur) {
        if (motif == null || motif.trim().length() < 20) {
            throw new MotifRouvreInvalideException("Le motif de réouverture doit faire au moins 20 caractères.");
        }

        Periode periode = periodeRepository.findByNumero(numero)
                .orElseThrow(() -> new NotFoundException("Trimestre " + numero + " non trouvé"));

        if (!periode.isCloture()) {
            Response<String> reponse = new Response<>("Le trimestre " + numero + " n'est pas clôturé.");
            reponse.setHasError(true);
            reponse.setStatus(new Status(HttpStatus.CONFLICT.value(), "Trimestre non clôturé"));
            return ResponseEntity.status(HttpStatus.CONFLICT).body(reponse);
        }

        boolean dernierCloture = true;
        for (int i = numero + 1; i <= 3; i++) {
            Periode trimestreSup = periodeRepository.findByNumero(i).orElse(null);
            if (trimestreSup != null && trimestreSup.isCloture()) {
                dernierCloture = false;
                break;
            }
        }

        if (!dernierCloture) {
            Response<String> reponse = new Response<>("Impossible de rouvrir le trimestre " + numero + " : un trimestre supérieur est clôturé.");
            reponse.setHasError(true);
            reponse.setStatus(new Status(HttpStatus.CONFLICT.value(), "Pas le dernier trimestre clôturé"));
            return ResponseEntity.status(HttpStatus.CONFLICT).body(reponse);
        }

        periode.setCloture(false);
        periode.setMotifRouvre(motif);
        periode.setDateRouvre(LocalDate.now());
        periode.setUtilisateurRouvre(auteur);
        periodeRepository.save(periode);

        Response<String> reponse = new Response<>("Trimestre " + numero + " réouvert avec succès. Motif: " + motif);
        reponse.setHasError(false);
        reponse.setNombre(1L);
        reponse.setStatus(new Status(800, "Trimestre rouvert"));
        return ResponseEntity.ok(reponse);
    }

    /**
     * Clôture un trimestre après vérification que chaque matière dans chaque classe dispose d'au moins une note.
     *
     * @param numero Numéro du trimestre (1, 2 ou 3).
     * @return Réponse HTTP confirmant la clôture ou listant les matières manquantes.
     */
    public ResponseEntity<Response<String>> clotureTrimestre(Integer numero) {
        Periode periode = periodeRepository.findByNumero(numero)
                .orElseThrow(() -> new NotFoundException("Trimestre " + numero + " non trouvé"));

        if (periode.isCloture()) {
            Response<String> reponse = new Response<>("Le trimestre " + numero + " est déjà clôturé.");
            reponse.setHasError(true);
            reponse.setStatus(new Status(HttpStatus.CONFLICT.value(), "Trimestre déjà clôturé"));
            return ResponseEntity.status(HttpStatus.CONFLICT).body(reponse);
        }

        List<String> manques = new ArrayList<>();
        List<Classe> classes = classeRepository.findAll();
        List<Matiere> matieres = matiereRepository.findAll();

        for (Classe classe : classes) {
            for (Matiere matiere : matieres) {
                long count = noteRepository.countNotesByClasseAndMatiereAndTrimestre(classe.getId(), matiere.getId(), numero);
                if (count == 0) {
                    manques.add("Classe " + classe.getNom() + " - Matière " + matiere.getNom());
                }
            }
        }

        if (!manques.isEmpty()) {
            Response<String> reponse = new Response<>("Impossible de clôturer le trimestre " + numero + " : notes manquantes.");
            reponse.setHasError(true);
            reponse.setNombre((long) manques.size());
            reponse.setData(manques);
            reponse.setStatus(new Status(HttpStatus.CONFLICT.value(), "Clôture refusée"));
            return ResponseEntity.status(HttpStatus.CONFLICT).body(reponse);
        }

        periode.setCloture(true);
        periodeRepository.save(periode);

        Response<String> reponse = new Response<>("Trimestre " + numero + " clôturé avec succès.");
        reponse.setHasError(false);
        reponse.setNombre(1L);
        reponse.setStatus(new Status(HttpStatus.OK.value(), "Trimestre clôturé"));
        return ResponseEntity.ok(reponse);
    }
}
