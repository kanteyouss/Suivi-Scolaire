package com.suiviScolaire.suiviScolaire.controller;

import com.suiviScolaire.suiviScolaire.service.MatiereService;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST exposant les endpoints de gestion des matières.
 */
@RestController
@RequestMapping("/matieres")
public class MatiereController extends CrudController {
    public MatiereController(MatiereService matiereService) {
        super(matiereService);
    }
}
