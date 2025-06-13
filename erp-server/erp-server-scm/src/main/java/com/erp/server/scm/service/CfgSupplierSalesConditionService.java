package com.erp.server.scm.service;
import com.erp.model.scm.entity.CfgSupplierSalesConditionEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.scm.dto.CfgSupplierSalesConditionDTO;

/**
 * <p>
 * 销量设置条件明细 服务类
 * </p>
 *
 * @author jack
 * @since 2025-06-13
 */
public interface CfgSupplierSalesConditionService extends SuperService<CfgSupplierSalesConditionEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-06-13
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgSupplierSalesConditionDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-06-13
    * @param dto
    * @return
    */
    Boolean update(CfgSupplierSalesConditionDTO.UpdateDTO dto);


}
