package com.erp.server.wms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.dto.CfgSettingVirtualDTO;

/**
 * 虚拟仓配置
 * @author will
 * @date 2024/9/23 16:08
 */
public interface CfgSettingVirtualService {
    /**
     * 添加虚拟仓配置
     * @author will
     * @date 2024/9/23 16:09
     * @param dto
     * @return AddDTO
     */
    BaseResultDTO.AddDTO addVirtual(CfgSettingVirtualDTO.AddDTO dto);
    /**
     * 虚拟仓配置查询
     * @author will
     * @date 2024/9/23 16:10
     * @return ViewDTO
     */
    CfgSettingVirtualDTO.ViewDTO viewVirtual();
}
