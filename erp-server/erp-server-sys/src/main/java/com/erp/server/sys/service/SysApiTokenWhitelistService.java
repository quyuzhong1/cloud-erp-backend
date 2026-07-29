package com.erp.server.sys.service;

import com.common.business.dto.base.BaseIdDTO;
import com.common.business.service.SuperService;
import com.erp.model.sys.dto.SysApiTokenWhitelistDTO;
import com.erp.model.sys.entity.SysApiTokenWhitelistEntity;

import java.util.List;

/**
 * <p>
 * API Token 接口白名单 服务类
 * </p>
 */
public interface SysApiTokenWhitelistService extends SuperService<SysApiTokenWhitelistEntity> {

    List<SysApiTokenWhitelistDTO.ListDTO> listConfig();

    Boolean add(SysApiTokenWhitelistDTO.AddDTO dto);

    Boolean update(SysApiTokenWhitelistDTO.UpdateDTO dto);

    Boolean removeConfig(BaseIdDTO dto);

    Boolean match(String requestPath);
}
