package com.erp.server.oms.service;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.dto.SkuMappingExtendDTO;
import com.erp.model.oms.entity.SkuMappingEntity;
import com.erp.model.oms.entity.SkuMappingExtendEntity;
import com.common.business.service.SuperService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * sku仓库发货配置 服务类
 * </p>
 *
 * @author Jim
 * @since 2024-02-27
 */
public interface SkuMappingExtendService extends SuperService<SkuMappingExtendEntity> {


    /**
     * 通过mainIds查询配置, 默认根据字典
     */
    List<SkuMappingExtendEntity> findByMainIds(List<String> mainIds);

    /**
     * 查询指定mainIds的sku仓库发货配置
     * @param mainIds 主表ID
     * @param defaultNullThrow 默认配置空抛异常
     * @return map
     */
    Map<String, List<SkuMappingExtendDTO.ListDTO>> mapByMainIds(List<String> mainIds, boolean defaultNullThrow);

    /**
     * 检查和保存配置
     */
    void checkAndSave(SkuMappingEntity entity, List<SkuMappingDTO.SkuMappingExtendListDTO> extendList);

    void copyBySkuMapping(SkuMappingEntity lastestSkuMapping, List<SkuMappingEntity> skuMappingEntityList);
}
