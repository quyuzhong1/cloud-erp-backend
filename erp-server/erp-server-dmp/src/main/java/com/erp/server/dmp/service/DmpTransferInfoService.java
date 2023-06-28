package com.erp.server.dmp.service;

import com.common.business.service.SuperService;
import com.erp.model.dmp.entity.DmpTransferInfoEntity;

/**
 * <p>
 * 直接调拨单 服务类
 * </p>
 *
 * @author Cloud
 * @since 2023-06-19
 */
public interface DmpTransferInfoService extends SuperService<DmpTransferInfoEntity> {
    /**
     * 新增
     */
    Boolean add(DmpTransferInfoEntity entity);
    /**
     * 修改
     */
    Boolean update(DmpTransferInfoEntity entity);

    /**
     * 检查订单存在则更新，不存在则新增
     * @param ext
     */
    void checkOrder(DmpTransferInfoEntity ext);
}
