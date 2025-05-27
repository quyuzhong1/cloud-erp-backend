package com.erp.server.workflow.service;
import com.erp.model.workflow.entity.ThirdProcessManagementEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.workflow.dto.ThirdProcessManagementDTO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author will
 * @since 2025-05-23
 */
public interface ThirdProcessManagementService extends SuperService<ThirdProcessManagementEntity> {

    /**
    * 新增
    * @author will
    * @date: 2025-05-23
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ThirdProcessManagementDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2025-05-23
    * @param dto
    * @return
    */
    Boolean update(ThirdProcessManagementDTO.UpdateDTO dto);


}
