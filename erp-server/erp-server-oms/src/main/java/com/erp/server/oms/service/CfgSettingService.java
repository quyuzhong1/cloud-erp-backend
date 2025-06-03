package com.erp.server.oms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.oms.dto.CfgSettingDTO;
import com.erp.model.oms.entity.CfgSettingEntity;

import java.util.List;

/**
 * <p>
 * 系统配置管理 服务类
 * </p>
 *
 * @author zdy
 * @since 2025-03-24
 */
public interface CfgSettingService extends SuperService<CfgSettingEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2025-03-24
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgSettingDTO.AddDTO dto);

    /**
     * 查询详情
     * @return
     */
    CfgSettingDTO.ViewDTO view();

    /**
     * 获取超时设置的配置
     * @return
     */
    CfgSettingDTO.ViewDTO getSetting(String key);

    /**
     * 获取配置实体记录
     * @param key
     * @return
     */
    CfgSettingEntity getSettingByKey(String key);


    /**
     * 获取配置实体记录
     * @param key
     * @return
     */
    List<CfgSettingEntity> listSettingByKey(String key);
}
