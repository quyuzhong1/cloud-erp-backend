package com.erp.server.mrp.service;
import com.erp.model.mrp.entity.CfgRuleWarehouseDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.mrp.dto.CfgRuleWarehouseDetailDTO;

/**
 * <p>
 * 仓库（规则设置）明细 服务类
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
public interface CfgRuleWarehouseDetailService extends SuperService<CfgRuleWarehouseDetailEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-08-23
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgRuleWarehouseDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-08-23
    * @param dto
    * @return
    */
    Boolean update(CfgRuleWarehouseDetailDTO.UpdateDTO dto);


}
