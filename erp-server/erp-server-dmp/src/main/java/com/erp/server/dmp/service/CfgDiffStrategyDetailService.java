package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.CfgDiffStrategyDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.CfgDiffStrategyDetailDTO;

/**
 * <p>
 * 差异策略配置明细 服务类
 * </p>
 *
 * @author shukai
 * @since 2025-11-11
 */
public interface CfgDiffStrategyDetailService extends SuperService<CfgDiffStrategyDetailEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2025-11-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgDiffStrategyDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2025-11-11
    * @param dto
    * @return
    */
    Boolean update(CfgDiffStrategyDetailDTO.UpdateDTO dto);


}
