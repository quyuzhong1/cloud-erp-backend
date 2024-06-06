package com.erp.server.dmp.service;

import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.dto.ThirdMappingDTO;

public interface ThirdMappingStrategy {
    boolean supports(String type);

    BaseResultDTO.AddDTO add(ThirdMappingDTO.AddDTO addDTO);

    ThirdMappingDTO.MappingViewDTO view(ThirdMappingDTO.ViewParamDTO viewParamDTO);
}

