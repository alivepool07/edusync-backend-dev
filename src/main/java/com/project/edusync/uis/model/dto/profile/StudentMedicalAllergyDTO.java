package com.project.edusync.uis.model.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentMedicalAllergyDTO {
    private Long id;
    private String allergy;
    private String severity; // e.g., "MILD", "SEVERE"
    private String notes;
}
