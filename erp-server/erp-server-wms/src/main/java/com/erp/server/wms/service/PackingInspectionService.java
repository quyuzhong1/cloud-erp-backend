package com.erp.server.wms.service;

import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.wms.dto.PackingInspectionDTO;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;

/**
 * <p>
 * 包装验货 服务类
 * </p>
 *
 * @author liuruipeng
 * @since 2023-12-13
 */
public interface PackingInspectionService {

    PackingInspectionDTO.ViewDTO scan(PackingInspectionDTO.ScanDTO dto);

    void reset(String id);

    /**
     * 自动出库
     * @author will
     * @date 2024/6/28 16:40
     * @param soB2cEntity
     * @param entity
     */
    void soB2cDeliveryAutoOut (SoB2cEntity soB2cEntity, SoB2cDeliveryEntity entity);
}
