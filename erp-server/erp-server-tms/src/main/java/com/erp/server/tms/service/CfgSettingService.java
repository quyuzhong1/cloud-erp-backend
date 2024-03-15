package com.erp.server.tms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.tms.dto.CfgSettingDTO;
import com.erp.model.tms.entity.CfgSettingEntity;

/**
 * <p>
 * 系统配置管理 服务类
 * </p>
 *
 * @author zdy
 * @since 2024-02-29
 */
public interface CfgSettingService extends SuperService<CfgSettingEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2024-02-29
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgSettingDTO.AddDTO dto);

    /**
     * 查询详情
     * @author Will
     * @date: 2024/1/11 14:35
     * @return ViewDTO
     */
    CfgSettingDTO.ViewDTO view(String id);

    /**
     * 根据key查询配置
     * @Author Luo_WG
     * @Date 2024/1/11 19:34
     * @param key
     * @return com.erp.model.wms.entity.CfgSettingEntity
     **/
    CfgSettingEntity getByKey(String key);
}
