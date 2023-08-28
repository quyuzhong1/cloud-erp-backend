package com.erp.server.oms.service;
import com.erp.model.oms.entity.RuleRefConditionEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.RuleRefConditionDTO;

/**
 * <p>
 * 规则关联条件表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
 */
public interface RuleRefConditionService extends SuperService<RuleRefConditionEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-08-28
    * @param dto
    * @return
    */
    String add(RuleRefConditionDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-08-28
    * @param dto
    * @return
    */
    Boolean update(RuleRefConditionDTO.UpdateDTO dto);


}
