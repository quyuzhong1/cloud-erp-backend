package com.erp.server.scm.service;
import com.erp.model.scm.entity.SupplierPlantAddrEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.scm.dto.SupplierPlantAddrDTO;

/**
 * <p>
 * 供应商工厂地信息 服务类
 * </p>
 *
 * @author will
 * @since 2025-07-21
 */
public interface SupplierPlantAddrService extends SuperService<SupplierPlantAddrEntity> {

    /**
    * 新增
    * @author will
    * @date: 2025-07-21
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SupplierPlantAddrDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2025-07-21
    * @param dto
    * @return
    */
    Boolean update(SupplierPlantAddrDTO.UpdateDTO dto);


}
