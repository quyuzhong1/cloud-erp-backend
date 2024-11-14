package com.erp.server.mrp.service;

import com.erp.model.mrp.dto.CfgDataArchivingDTO;
import com.erp.model.mrp.entity.CfgDataArchivingEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * 归档配置 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-09-26
 */
public interface CfgDataArchivingService extends SuperService<CfgDataArchivingEntity> {

    /**
     * 获取归档配置
     */
    List<CfgDataArchivingEntity> getEffectiveData();

    /**
     * 根据配置归档对应表所有数据
     * @param config 配置
     */
    void archiveData(CfgDataArchivingEntity config);
    /**
     * 根据配置归档对应表数据
     * @param config 配置
     */
    void archiveData(CfgDataArchivingEntity config, String detailId);

    /**
     * 保存数据
     * @param dto 参数
     */
    void saveData(CfgDataArchivingDTO dto);
}
