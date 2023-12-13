package com.erp.server.oms.service;
import com.common.core.controller.vo.ApiResult;
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

    /**
     * 根据key
     * @author yl
     * @date 2023-08-31 11:48
     * @param type
     * @return java.util.List<com.common.business.dto.base.BaseDropDownDTO.CommonDTO>
     */
    List<BaseDropDownDTO.CommonDTO> listByType(String type);

    /**
     * 根据typeList 获取对应数据
     * @author yl
     * @date 2023-08-31 11:48
     * @param typeList
     * @return java.util.List<com.common.business.dto.base.BaseDropDownDTO.CommonDTO>
     */
    List<DictRuleConditionEntity> listDbByTypes(List<String> typeList);

    List<BaseDropDownDTO.CommonDTO> listRuleField();
}
