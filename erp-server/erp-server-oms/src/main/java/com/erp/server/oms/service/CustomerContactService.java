package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.dto.CustomerContactDTO;
import com.erp.model.oms.entity.CustomerContactEntity;

import java.util.List;

/**
 * <p>
 * 客户联系人信息 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface CustomerContactService extends SuperService<CustomerContactEntity> {

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
}
