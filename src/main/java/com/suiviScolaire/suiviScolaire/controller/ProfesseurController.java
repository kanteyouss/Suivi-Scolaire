package com.suiviScolaire.suiviScolaire.controller;

import com.suiviScolaire.suiviScolaire.service.ProfesseurService;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST exposant les endpoints de gestion des professeurs.
 */
@RestController
@RequestMapping("/professeurs")
public class ProfesseurController extends CrudController {
    public ProfesseurController(ProfesseurService professeurService) {
        super(professeurService);
    }
}
