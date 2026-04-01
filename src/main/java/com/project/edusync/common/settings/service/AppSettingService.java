package com.project.edusync.common.settings.service;

import com.project.edusync.common.settings.model.dto.AppSettingRequestDto;
import com.project.edusync.common.settings.model.dto.AppSettingResponseDto;

import java.util.List;

public interface AppSettingService {

    List<AppSettingResponseDto> getAllSettings(boolean revealSecrets);

    AppSettingResponseDto getSetting(String key, boolean revealSecrets);

    AppSettingResponseDto upsertSetting(AppSettingRequestDto requestDto);

    List<AppSettingResponseDto> upsertBulk(List<AppSettingRequestDto> requestDtos);

    String getDecryptedValue(String key, String defaultValue);
}

