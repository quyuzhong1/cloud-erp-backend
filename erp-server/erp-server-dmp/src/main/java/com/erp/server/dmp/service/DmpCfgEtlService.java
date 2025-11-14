package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpCfgEtlEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpCfgEtlDTO;

/**
 * <p>
 * etl配置信息 服务类
 * </p>
 *
 * @author shukai
 * @since 2025-07-21
 */
public interface DmpCfgEtlService extends SuperService<DmpCfgEtlEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2025-07-21
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpCfgEtlDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2025-07-21
    * @param dto
    * @return
    */
    Boolean update(DmpCfgEtlDTO.UpdateDTO dto);


}
