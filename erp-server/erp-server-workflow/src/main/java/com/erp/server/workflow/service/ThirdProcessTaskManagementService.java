package com.erp.server.workflow.service;
import com.erp.model.workflow.entity.ThirdProcessTaskManagementEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.workflow.dto.ThirdProcessTaskManagementDTO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author will
 * @since 2025-05-23
 */
public interface ThirdProcessTaskManagementService extends SuperService<ThirdProcessTaskManagementEntity> {

    /**
    * 新增
    * @author will
    * @date: 2025-05-23
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(List<ThirdProcessTaskManagementDTO.AddDTO> dto);

    /**
    * 修改
    * @author will
    * @date: 2025-05-23
    * @param dto
    * @return
    */
    Boolean update(List<ThirdProcessTaskManagementDTO.UpdateDTO> dto);


}
