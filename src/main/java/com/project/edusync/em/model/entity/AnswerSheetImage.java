package com.project.edusync.em.model.entity;

import com.project.edusync.common.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "answer_sheet_images", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"answer_sheet_id", "page_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@AttributeOverride(name = "id", column = @Column(name = "answer_sheet_image_id"))
public class AnswerSheetImage extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "answer_sheet_id", nullable = false)
    private AnswerSheet answerSheet;

    @Column(name = "page_number", nullable = false)
    private Integer pageNumber;

    @Column(name = "image_url", nullable = false, length = 1000)
    private String imageUrl;
}

