package com.erp.server.sys.service;

import com.common.business.dto.base.BaseIdDTO;
import com.common.business.service.SuperService;
import com.erp.model.sys.dto.SysApiTokenDTO;
import com.erp.model.sys.entity.SysApiTokenEntity;

import java.util.List;

/**
 * <p>
 * 个人访问令牌 服务类
 * </p>
 */
public interface SysApiTokenService extends SuperService<SysApiTokenEntity> {

    List<SysApiTokenDTO.ListDTO> listCurrentUserToken();

    SysApiTokenDTO.TokenDTO add(SysApiTokenDTO.AddDTO dto);

    Boolean update(SysApiTokenDTO.UpdateDTO dto);

    Boolean removeToken(BaseIdDTO dto);

    SysApiTokenDTO.TokenDTO copy(String id);

    SysApiTokenDTO.ValidateRespDTO validate(SysApiTokenDTO.ValidateReqDTO dto);
}
