package com.suiviScolaire.suiviScolaire.controller;

import com.suiviScolaire.suiviScolaire.service.NoteService;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST exposant les endpoints de gestion des notes.
 */
@RestController
@RequestMapping("/notes")
public class NoteController extends CrudController {
    public NoteController(NoteService noteService) {
        super(noteService);
    }
}
