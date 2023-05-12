package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.dto.CustomerAddressDTO;
import com.erp.model.oms.dto.CustomerContactDTO;
import com.erp.model.oms.entity.CustomerAddressEntity;

import java.util.List;

/**
 * <p>
 * 客户地址信息 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface CustomerAddressService extends SuperService<CustomerAddressEntity> {

    
    /**
     * 检查默认地址是否存在多个
     * @author yl
     * @date 2023-05-12 14:55
     * @param addressList
     * @return void
     */
    void checkIsDefault(List<CustomerAddressDTO.AddDTO> addressList);

    /**
     * 批量保存地址信息
     * @author yl
     * @date 2023-05-12 15:56
     * @param id
     * @param contactList
     * @return void
     */
    void saveBatchAddress(String id, List<CustomerContactDTO.AddDTO> contactList);
}
