package com.erp.server.oms.service;
import com.erp.model.oms.entity.RuleConditionEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.RuleConditionDTO;

/**
 * <p>
 * 规则条件表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
 */
public interface RuleConditionService extends SuperService<RuleConditionEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-08-28
    * @param dto
    * @return
    */
    String add(RuleConditionDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-08-28
    * @param dto
    * @return
    */
    Boolean update(RuleConditionDTO.UpdateDTO dto);


}
