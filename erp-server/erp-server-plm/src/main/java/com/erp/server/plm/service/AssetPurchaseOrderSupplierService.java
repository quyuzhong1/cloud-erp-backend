package com.erp.server.plm.service;
import com.erp.model.plm.entity.AssetPurchaseOrderSupplierEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.plm.dto.AssetPurchaseOrderSupplierDTO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wtr
 * @since 2025-10-16
 */
public interface AssetPurchaseOrderSupplierService extends SuperService<AssetPurchaseOrderSupplierEntity> {

    /**
    * 新增
    * @author wtr
    * @date: 2025-10-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AssetPurchaseOrderSupplierDTO.AddDTO dto);

    /**
    * 修改
    * @author wtr
    * @date: 2025-10-16
    * @param dto
    * @return
    */
    Boolean update(AssetPurchaseOrderSupplierDTO.UpdateDTO dto);


}
