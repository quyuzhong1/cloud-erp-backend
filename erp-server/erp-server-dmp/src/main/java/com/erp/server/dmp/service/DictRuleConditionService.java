package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DictRuleConditionEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DictRuleConditionDTO;

import java.util.List;

/**
 * <p>
 * 条件字典表 服务类
 * </p>
 *
 * @author lrp
 * @since 2025-01-20
 */
public interface DictRuleConditionService extends SuperService<DictRuleConditionEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2025-01-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DictRuleConditionDTO.AddDTO dto);

    /**
    * 修改
    * @author lrp
    * @date: 2025-01-20
    * @param dto
    * @return
    */
    Boolean update(DictRuleConditionDTO.UpdateDTO dto);


    List<DictRuleConditionEntity> listDbByTypes(List<String> list);

    List<BaseDropDownDTO.CommonDTO> listRuleField();
}
