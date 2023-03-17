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

    /**
     * 检查联系人 是否有多个默认人
     * @author yl
     * @date 2023-03-17 16:39
     * @param contactList
     * @return void
     */
    void checkIsDefault(List<SupplierContactDTO.AddDTO> contactList);
}
