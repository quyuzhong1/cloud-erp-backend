package com.erp.server.wms.service;

import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * 销售订单出库单 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface SoOutstockService extends SuperService<SoOutstockEntity> {

    /**
     * 根据来源id查询出库表
     * @Author Luo_WG
     * @Date 2023/5/12 12:18
     * @param ids
     * @return java.util.List<com.erp.model.oms.entity.SoOutstockEntity>
     **/
    List<SoOutstockEntity> listBySourceId(List<String> ids);
}
