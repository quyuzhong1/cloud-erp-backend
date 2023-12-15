package com.erp.server.wms.service;
import com.erp.model.wms.entity.SoB2cDeliveryDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SoB2cDeliveryDetailDTO;

import java.util.List;

/**
 * <p>
 * b2c发货单详情 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-12-13
 */
public interface SoB2cDeliveryDetailService extends SuperService<SoB2cDeliveryDetailEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-12-13
    * @param entities
    * @param mainId
    * @return
    */
    void add(List<SoB2cDeliveryDetailEntity> entities, String mainId);

    List<SoB2cDeliveryDetailEntity> listByMainId(String mainId);
}
