package com.erp.server.oms.service;

import com.common.business.dto.PlatformOrderDTO;
import com.common.business.service.SuperService;
import com.erp.model.oms.dto.CustomerAddressDTO;
import com.erp.model.oms.entity.*;

import java.util.List;

/**
 * <p>
 * 客户地址信息 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface CustomerB2cAddressService extends SuperService<CustomerB2cAddressEntity> {

    
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
    void saveBatchAddress(String id, List<CustomerAddressDTO.AddDTO> contactList);

    
    /**
     * 根据主表id 获取地址信息
     * @author yl
     * @date 2023-05-15 10:01
     * @param mainId
     * @return java.util.List<com.erp.model.oms.dto.CustomerAddressDTO.ViewDTO>
     */
    List<CustomerAddressDTO.ViewDTO> listByMainId(String mainId);
    /**
     * 修改地址信息
     * @author yl
     * @date 2023-05-15 11:08
     * @param mainId
     * @param addressList
     * @return void
     */
    void updateBatchAddress(String mainId, List<CustomerAddressDTO.ViewDTO> addressList);
    /**
     * @description: 根据id查询
     * @author Will
     * @date: 2023/7/13 12:09
     * @param customerAddressId
     * @return ViewDTO
     */
    CustomerAddressDTO.ViewDTO getCustomerAddressById(String customerAddressId);

    void saveOrUpdateEntity(PlatformOrderDTO dto, CustomerB2cEntity mainEntity, SoB2cReceiverEntity receiverEntity ,boolean notUpdateAddress);

    CustomerB2cAddressEntity getByMainId(String mainId);
}
