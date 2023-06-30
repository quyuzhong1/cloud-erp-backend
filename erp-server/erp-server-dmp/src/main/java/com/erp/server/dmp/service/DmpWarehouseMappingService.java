package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpWarehouseMappingEntity;
import com.common.business.service.SuperService;

import java.util.List;
import java.util.Map;


/**
 * <p>
 * 平台仓库映射表 服务类
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-06-27
 */
public interface DmpWarehouseMappingService extends SuperService<DmpWarehouseMappingEntity> {

    /**
     * 根据仓库编码获取仓库映射信息
     * @param warehouseCode
     * @return
     */
    DmpWarehouseMappingEntity getByWarehouseCode(String warehouseCode);

    /**
     * 根据多个仓库编码获取仓库映射信息
     * @param warehouseCodes
     * @return
     */
    Map<String, DmpWarehouseMappingEntity> getByWarehouseCodes(List<String> warehouseCodes);


    /**
     * 根据平台仓库id和平台类型获取仓库映射信息
     * @param sourceId
     * @param platform
     * @return
     */
    DmpWarehouseMappingEntity getSourceWarehouseId(String sourceId, String platform);


}
