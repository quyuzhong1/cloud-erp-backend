package com.erp.server.oms.dht;

import com.erp.model.oms.dto.CustomerDTO;
import com.erp.model.oms.entity.CustomerInfoEntity;

public interface DhtService {

    /**
     * 通过客户编号查询客户账户信息
     */
    CustomerDTO.ThirdCustomerAccountDTO queryCustomerAccountByCustomerCode(CustomerInfoEntity customerInfoEntity);
}
