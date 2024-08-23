package com.erp.server.mrp.service;
import com.erp.model.mrp.entity.CfgRuleLogisticsDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.mrp.dto.CfgRuleLogisticsDetailDTO;

/**
 * <p>
 * 备货物流明细（规则设置） 服务类
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
public interface CfgRuleLogisticsDetailService extends SuperService<CfgRuleLogisticsDetailEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-08-23
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgRuleLogisticsDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-08-23
    * @param dto
    * @return
    */
    Boolean update(CfgRuleLogisticsDetailDTO.UpdateDTO dto);


}
