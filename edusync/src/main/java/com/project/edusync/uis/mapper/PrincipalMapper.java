package com.project.edusync.uis.mapper;

import com.project.edusync.iam.model.dto.CreatePrincipalRequestDTO;
import com.project.edusync.uis.model.entity.details.PrincipalDetails;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {JsonMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PrincipalMapper {

    @Mapping(target = "schoolLevelManaged", source = "schoolLevelManaged")
    // JsonMapper handles List<String> -> String (JSON)
    @Mapping(target = "administrativeCertifications", source = "administrativeCertifications")
    PrincipalDetails toEntity(CreatePrincipalRequestDTO dto);
}