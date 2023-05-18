package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.entity.TransferOutDetailEntity;

import java.util.List;

/**
 * <p>
 * 分布式调出单明细 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface TransferOutDetailService extends SuperService<TransferOutDetailEntity> {
    /**
     * @description: 根据来源ids查询
     * @author Will
     * @date: 2023/5/18 9:44
     * @param sourceDetailIds
     * @return List<TransferOutDetailEntity>
     */
    List<TransferOutDetailEntity> listSourceDetailIds(List<String> sourceDetailIds);
}
