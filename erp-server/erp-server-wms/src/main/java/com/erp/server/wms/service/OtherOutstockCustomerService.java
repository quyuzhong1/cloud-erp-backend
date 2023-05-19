package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.OtherOutstockCustomerDTO;
import com.erp.model.wms.entity.OtherOutstockCustomerEntity;

/**
 * <p>
 * 其他出库客户表 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface OtherOutstockCustomerService extends SuperService<OtherOutstockCustomerEntity> {
    /**
     * @description: 其他出库客户新增
     * @author Will
     * @date: 2023/5/19 16:29
     * @param otherOutstockCustomer
     * @param mainId
     */
    void add(OtherOutstockCustomerDTO.AddDTO otherOutstockCustomer, String mainId);
    /**
     * @description: 其他出库客户修改
     * @author Will
     * @date: 2023/5/19 16:33
     * @param otherOutstockCustomer
     */
    void update(OtherOutstockCustomerDTO.UpdateDTO otherOutstockCustomer);
}
