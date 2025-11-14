package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpEtlTaskEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpEtlTaskDTO;

/**
 * <p>
 * etl任务 服务类
 * </p>
 *
 * @author shukai
 * @since 2025-07-21
 */
public interface DmpEtlTaskService extends SuperService<DmpEtlTaskEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2025-07-21
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpEtlTaskDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2025-07-21
    * @param dto
    * @return
    */
    Boolean update(DmpEtlTaskDTO.UpdateDTO dto);


}
