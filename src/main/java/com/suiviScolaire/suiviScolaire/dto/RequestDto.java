package com.suiviScolaire.suiviScolaire.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
        @JsonSubTypes.Type(ClasseRequestDto.class),
        @JsonSubTypes.Type(EleveRequestDto.class),
        @JsonSubTypes.Type(MatiereRequestDto.class),
        @JsonSubTypes.Type(NoteRequestDto.class),
        @JsonSubTypes.Type(ProfesseurRequestDto.class)

})
public interface RequestDto { }
