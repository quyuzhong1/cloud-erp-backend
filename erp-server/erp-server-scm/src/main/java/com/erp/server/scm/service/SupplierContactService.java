package com.erp.server.scm.service;

import com.common.core.serveice.SuperService;
import com.erp.model.scm.dto.SupplierContactDTO;
import com.erp.model.scm.entity.SupplierContactEntity;

import java.util.List;

/**
 * <p>
 * 供应商联系人表 服务类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
public interface SupplierContactService extends SuperService<SupplierContactEntity> {

    
    /**
     * 保存供应商联系信息
     * @author yl
     * @date 2023-03-17 16:07
     * @param supplierId
     * @param contactList
     * @return void
     */
    void saveBatchContact(String supplierId, List<SupplierContactDTO.AddDTO> contactList);
}
