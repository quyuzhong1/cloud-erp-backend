package com.erp.server.oms.service;
import com.erp.model.oms.entity.CfgConditionEntity;
import com.common.business.service.SuperService;
import com.erp.model.oms.dto.CfConditionDTO;

import java.util.List;

/**
 * <p>
 * 条件配置表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-30
 */
public interface CfgConditionService extends SuperService<CfgConditionEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-08-30
    * @param dto
    * @return
    */
    String add(CfConditionDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-08-30
    * @param dto
    * @return
    */
    Boolean update(CfConditionDTO.UpdateDTO dto);

    /**
     * 根据添加code 获取到逻辑关系
     * @param conditionCode
     * @return
     */
    List<CfConditionDTO.CommonDTO> listByConditionCode(String conditionCode);
}
