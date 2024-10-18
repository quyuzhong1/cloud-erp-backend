package com.erp.server.mrp.service;
import com.common.business.service.SuperService;
import com.erp.model.mrp.dto.CfgRuleOrderStrategyDTO;
import com.erp.model.mrp.entity.CfgRuleOrderStrategyEntity;

/**
 * <p>
 * 策略（规则设置） 服务类
 * </p>
 *
 * @author will
 * @since 2024-10-12
 */
public interface CfgRuleOrderStrategyService extends SuperService<CfgRuleOrderStrategyEntity> {

    /**
    * 修改
    * @author will
    * @date: 2024-10-12
    * @param dto
    * @return
    */
    Boolean update(CfgRuleOrderStrategyDTO.UpdateDTO dto);

    /**
     * 查询详情
     * @author will
     * @date 2024/10/12 11:24
     * @return ViewDTO
     */
    CfgRuleOrderStrategyDTO.ViewDTO view();
}
