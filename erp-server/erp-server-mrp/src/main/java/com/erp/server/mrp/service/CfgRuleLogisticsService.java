package com.erp.server.mrp.service;
import com.erp.model.mrp.entity.CfgRuleLogisticsEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.mrp.dto.CfgRuleLogisticsDTO;

/**
 * <p>
 * 备货物流（规则设置） 服务类
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
public interface CfgRuleLogisticsService extends SuperService<CfgRuleLogisticsEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-08-23
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgRuleLogisticsDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-08-23
    * @param dto
    * @return
    */
    Boolean update(CfgRuleLogisticsDTO.UpdateDTO dto);


}
