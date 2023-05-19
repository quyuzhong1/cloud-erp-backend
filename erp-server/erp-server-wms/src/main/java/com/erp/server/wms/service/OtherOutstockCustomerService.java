package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.OtherOutstockCustomerDTO;
import com.erp.model.wms.entity.OtherOutstockCustomerEntity;

import java.util.List;

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
     * @param mainId
     */
    void update(OtherOutstockCustomerDTO.UpdateDTO otherOutstockCustomer,String mainId);
    /**
     * @description: 根据主表ids删除客户
     * @author Will
     * @date: 2023/5/19 19:22
     * @param mainIds
     */
    void removeByMainIds(List<String> mainIds);
    /**
     * @description: mainId
     * @author Will
     * @date: 2023/5/19 19:25
     * @param mainId
     */
    OtherOutstockCustomerEntity getByMainId(String mainId);
}
