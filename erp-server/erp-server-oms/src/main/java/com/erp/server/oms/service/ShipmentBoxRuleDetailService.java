package com.erp.server.oms.service;
import com.erp.model.oms.entity.ShipmentBoxRuleDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.ShipmentBoxRuleDetailDTO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wtr
 * @since 2025-11-24
 */
public interface ShipmentBoxRuleDetailService extends SuperService<ShipmentBoxRuleDetailEntity> {

    /**
    * 新增
    * @author wtr
    * @date: 2025-11-24
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ShipmentBoxRuleDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author wtr
    * @date: 2025-11-24
    * @param dto
    * @return
    */
    Boolean update(ShipmentBoxRuleDetailDTO.UpdateDTO dto);


}
