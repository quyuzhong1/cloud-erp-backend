package com.erp.server.oms.service;
import com.erp.model.oms.entity.RuleDeliveryWarehouseEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.RuleDeliveryWarehouseDTO;

/**
 * <p>
 * 发货仓库规则表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
 */
public interface RuleDeliveryWarehouseService extends SuperService<RuleDeliveryWarehouseEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-08-28
    * @param dto
    * @return
    */
    String add(RuleDeliveryWarehouseDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-08-28
    * @param dto
    * @return
    */
    Boolean update(RuleDeliveryWarehouseDTO.UpdateDTO dto);


}
