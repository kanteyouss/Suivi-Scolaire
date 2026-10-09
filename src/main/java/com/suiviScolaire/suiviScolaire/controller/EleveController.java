package com.suiviScolaire.suiviScolaire.controller;

import com.suiviScolaire.suiviScolaire.service.EleveService;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST exposant les endpoints de gestion des élèves.
 */
@RestController
@RequestMapping("/eleves")
public class EleveController extends CrudController {
    public EleveController(EleveService eleveService) {
        super(eleveService);
    }
}
