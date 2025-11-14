package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.CfgDiffStrategyConditionEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.CfgDiffStrategyConditionDTO;

/**
 * <p>
 * 差异策略配置条件 服务类
 * </p>
 *
 * @author shukai
 * @since 2025-11-11
 */
public interface CfgDiffStrategyConditionService extends SuperService<CfgDiffStrategyConditionEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2025-11-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgDiffStrategyConditionDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2025-11-11
    * @param dto
    * @return
    */
    Boolean update(CfgDiffStrategyConditionDTO.UpdateDTO dto);


}
