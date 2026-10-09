package com.suiviScolaire.suiviScolaire.controller;

import com.suiviScolaire.suiviScolaire.service.ClasseService;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST exposant les endpoints de gestion des classes.
 */
@RestController
@RequestMapping("/classes")
public class ClasseController extends CrudController {
    public ClasseController(ClasseService classeService) {
        super(classeService);
    }
}
