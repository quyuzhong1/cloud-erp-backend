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

    /**
     * 根据供应商id 获取到联系人信息
     * @author yl
     * @date 2023-03-20 10:07
     * @param supplierId
     * @return java.util.List<com.erp.model.scm.dto.SupplierContactDTO.UpdateDTO>
     */
    List<SupplierContactDTO.UpdateDTO> getBySupplierId(String supplierId);

    
    /**
     * 修改供应商联系人信息
     * @author yl
     * @date 2023-03-20 11:11
     * @param contactList
     * @param supplierId 供应商id
     * @return void
     */
    void updateSupplierContact(List<SupplierContactDTO.UpdateDTO> contactList,String supplierId);

    /**
     * 根据供应商id集合删除供应商联系 人
     * @author yl
     * @date 2023-03-20 18:44
     * @param supplierIds
     * @return void
     */
    void removeBySupplierIds(List<String> supplierIds);
}
