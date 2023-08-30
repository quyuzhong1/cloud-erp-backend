package com.erp.server.oms.service;
import com.erp.model.oms.entity.DictRuleConditionEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.DictRuleConditionDTO;

import java.util.List;

/**
 * <p>
 * 条件字典表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-30
 */
public interface DictRuleConditionService extends SuperService<DictRuleConditionEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-08-30
    * @param dto
    * @return
    */
    String add(DictRuleConditionDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-08-30
    * @param dto
    * @return
    */
    Boolean update(DictRuleConditionDTO.UpdateDTO dto);


    /**
     * 批量保存或者修改
     * @author yl
     * @date 2023-08-30 16:04
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean batchSaveOrUpdate(List<DictRuleConditionDTO.UpdateDTO> dto);
}
