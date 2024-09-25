package com.erp.server.plm.service;
import com.erp.model.plm.entity.PilotApplicationRefTaskEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.plm.dto.PilotApplicationRefTaskDTO;

/**
 * <p>
 * 试产/量产 关联任务 服务类
 * </p>
 *
 * @author tmj
 * @since 2024-08-27
 */
public interface PilotApplicationRefTaskService extends SuperService<PilotApplicationRefTaskEntity> {

    /**
    * 新增
    * @author tmj
    * @date: 2024-08-27
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(PilotApplicationRefTaskDTO.AddDTO dto);

    /**
    * 修改
    * @author tmj
    * @date: 2024-08-27
    * @param dto
    * @return
    */
    Boolean update(PilotApplicationRefTaskDTO.UpdateDTO dto);


}
