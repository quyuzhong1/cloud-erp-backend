package com.erp.server.scm.service;
import com.erp.model.scm.entity.SupplierPurchaseQuantityEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.scm.dto.SupplierPurchaseQuantityDTO;

/**
 * <p>
 * 供应商采购数量 服务类
 * </p>
 *
 * @author jack
 * @since 2025-06-18
 */
public interface SupplierPurchaseQuantityService extends SuperService<SupplierPurchaseQuantityEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-06-18
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SupplierPurchaseQuantityDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-06-18
    * @param dto
    * @return
    */
    Boolean update(SupplierPurchaseQuantityDTO.UpdateDTO dto);


}
