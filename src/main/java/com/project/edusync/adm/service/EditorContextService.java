package com.project.edusync.adm.service;

import com.project.edusync.adm.model.dto.response.EditorContextResponseDto;

import java.util.UUID;

public interface EditorContextService {
    EditorContextResponseDto getEditorContext(UUID sectionId);
}

