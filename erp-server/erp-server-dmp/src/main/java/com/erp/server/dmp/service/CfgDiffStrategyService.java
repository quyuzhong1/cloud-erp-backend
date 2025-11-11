package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.CfgDiffStrategyEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.CfgDiffStrategyDTO;

/**
 * <p>
 * 差异策略配置基础信息 服务类
 * </p>
 *
 * @author shukai
 * @since 2025-11-11
 */
public interface CfgDiffStrategyService extends SuperService<CfgDiffStrategyEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2025-11-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgDiffStrategyDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2025-11-11
    * @param dto
    * @return
    */
    Boolean update(CfgDiffStrategyDTO.UpdateDTO dto);


}
