package com.erp.server.oms.service;

import com.common.business.dto.PlatformOrderDTO;
import com.common.business.service.SuperService;
import com.erp.model.oms.dto.CustomerContactDTO;
import com.erp.model.oms.entity.CustomerB2cContactEntity;
import com.erp.model.oms.entity.CustomerB2cEntity;
import com.erp.model.oms.entity.SoB2cReceiverEntity;

import java.util.List;

/**
 * <p>
 * 客户联系人信息 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface CustomerB2cContactService extends SuperService<CustomerB2cContactEntity> {

    /**
     * 检查客户默认联系人是否多个
     * @author yl
     * @date 2023-05-12 14:00
     * @param contactList
     * @return void
     */
    void checkIsDefault(List<CustomerContactDTO.AddDTO> contactList);

    
    /**
     * 保存联系人信息
     * @author yl
     * @date 2023-05-12 15:53
     * @param id
     * @param contactList
     * @return void
     */
    void saveBatchContact(String id, List<CustomerContactDTO.AddDTO> contactList);

    
    
    /**
     *
     * 获取联系人信息
     * @author yl
     * @date 2023-05-15 9:53
     * @param mainId
     * @return java.util.List<com.erp.model.oms.dto.CustomerContactDTO.ViewDTO>
     */
    List<CustomerContactDTO.ViewDTO> listByMainId(String mainId);

    /**
     * 获取联系人信息
     * @Author Luo_WG
     * @Date 2023/6/9 14:34
     * @param mainId mainId
     * @return java.util.List<com.erp.model.oms.entity.CustomerContactEntity>
     **/
    List<CustomerB2cContactEntity> listEntityByMainId(String mainId);

    
    /**
     * 修改联系人信息
     * @author yl
     * @date 2023-05-15 10:57
     * @param mainId
     * @param contactList
     * @return void
     */
    void updateBatchContact(String mainId, List<CustomerContactDTO.ViewDTO> contactList);

    void saveOrUpdateEntity(PlatformOrderDTO dto, CustomerB2cEntity mainEntity, SoB2cReceiverEntity receiverEntity ,boolean notUpdateAddress);

    CustomerB2cContactEntity getByMainId(String mainId);
}
