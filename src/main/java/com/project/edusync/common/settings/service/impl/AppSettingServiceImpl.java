package com.project.edusync.common.settings.service.impl;

import com.project.edusync.common.settings.model.dto.AppSettingRequestDto;
import com.project.edusync.common.settings.model.dto.AppSettingResponseDto;
import com.project.edusync.common.settings.model.entity.AppSetting;
import com.project.edusync.common.settings.repository.AppSettingRepository;
import com.project.edusync.common.settings.security.AppSettingCryptoService;
import com.project.edusync.common.settings.service.AppSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AppSettingServiceImpl implements AppSettingService {

    private static final String MASKED_SECRET = "********";

    private final AppSettingRepository appSettingRepository;
    private final AppSettingCryptoService appSettingCryptoService;

    @Override
    @Transactional(readOnly = true)
    public List<AppSettingResponseDto> getAllSettings(boolean revealSecrets) {
        return appSettingRepository.findAll().stream()
                .sorted((a, b) -> a.getSettingKey().compareToIgnoreCase(b.getSettingKey()))
                .map(setting -> toDto(setting, revealSecrets))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AppSettingResponseDto getSetting(String key, boolean revealSecrets) {
        return appSettingRepository.findBySettingKey(key)
                .map(setting -> toDto(setting, revealSecrets))
                .orElse(new AppSettingResponseDto(null, key, null, false, null, null, null));
    }

    @Override
    @Transactional
    public AppSettingResponseDto upsertSetting(AppSettingRequestDto requestDto) {
        AppSetting setting = appSettingRepository.findBySettingKey(requestDto.key())
                .orElseGet(AppSetting::new);

        setting.setSettingKey(requestDto.key().trim());
        setting.setCategory(requestDto.category());
        setting.setDescription(requestDto.description());

        boolean encrypt = requestDto.encrypted() != null
                ? requestDto.encrypted()
                : isSensitiveKey(requestDto.key());
        setting.setEncrypted(encrypt);
        setting.setSettingValue(encrypt
                ? appSettingCryptoService.encrypt(requestDto.value())
                : requestDto.value());

        AppSetting saved = appSettingRepository.save(setting);
        return toDto(saved, false);
    }

    @Override
    @Transactional
    public List<AppSettingResponseDto> upsertBulk(List<AppSettingRequestDto> requestDtos) {
        return requestDtos.stream().map(this::upsertSetting).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public String getDecryptedValue(String key, String defaultValue) {
        return appSettingRepository.findBySettingKey(key)
                .map(setting -> setting.isEncrypted()
                        ? appSettingCryptoService.decrypt(setting.getSettingValue())
                        : setting.getSettingValue())
                .orElse(defaultValue);
    }

    private AppSettingResponseDto toDto(AppSetting setting, boolean revealSecrets) {
        String value;
        if (!setting.isEncrypted()) {
            value = setting.getSettingValue();
        } else if (revealSecrets) {
            value = appSettingCryptoService.decrypt(setting.getSettingValue());
        } else {
            value = MASKED_SECRET;
        }

        return new AppSettingResponseDto(
                setting.getUuid(),
                setting.getSettingKey(),
                value,
                setting.isEncrypted(),
                setting.getCategory(),
                setting.getDescription(),
                setting.getUpdatedAt()
        );
    }

    private boolean isSensitiveKey(String key) {
        String normalized = key == null ? "" : key.toLowerCase(Locale.ROOT);
        return normalized.contains("password")
                || normalized.contains("secret")
                || normalized.contains("token")
                || normalized.contains("api_key")
                || normalized.contains("access_key")
                || normalized.contains("private");
    }
}

