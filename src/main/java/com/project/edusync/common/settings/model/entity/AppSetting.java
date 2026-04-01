package com.project.edusync.common.settings.model.entity;

import com.project.edusync.common.model.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "app_settings")
@Getter
@Setter
@NoArgsConstructor
public class AppSetting extends AuditableEntity {

    @Column(name = "setting_key", nullable = false, unique = true, length = 150)
    private String settingKey;

    @Column(name = "setting_value", nullable = false, length = 4000)
    private String settingValue;

    @Column(name = "is_encrypted", nullable = false)
    private boolean encrypted;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "description", length = 500)
    private String description;
}

