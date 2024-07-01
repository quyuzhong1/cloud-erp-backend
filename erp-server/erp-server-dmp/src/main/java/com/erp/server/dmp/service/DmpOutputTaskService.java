package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpOutputTaskEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpOutputTaskDTO;

/**
 * <p>
 * 推送任务 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-07-01
 */
public interface DmpOutputTaskService extends SuperService<DmpOutputTaskEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-07-01
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpOutputTaskDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-07-01
    * @param dto
    * @return
    */
    Boolean update(DmpOutputTaskDTO.UpdateDTO dto);

    boolean updateErrorStatus(String id , boolean errorFlag , Integer errorCount , Exception e);
}
