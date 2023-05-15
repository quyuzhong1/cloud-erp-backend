package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.entity.TransferOutEntity;

import java.util.List;

/**
 * <p>
 * 分布式调出单 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface TransferOutService extends SuperService<TransferOutEntity> {
    /**
     * @description: 根据来源单据ids查询
     * @author Will
     * @date: 2023/5/15 9:25
     * @param ids
     * @return List<TransferOutEntity>
     */
    List<TransferOutEntity> listBySourceIds(List<String> ids);
}
