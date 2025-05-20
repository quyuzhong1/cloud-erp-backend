package com.erp.server.workflow.service;
import com.erp.model.workflow.entity.ApproveSyncRecordEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.workflow.dto.ApproveSyncRecordDTO;

/**
 * <p>
 * ERP审批同步-通知配置 服务类
 * </p>
 *
 * @author jack
 * @since 2025-05-12
 */
public interface ApproveSyncRecordService extends SuperService<ApproveSyncRecordEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-05-12
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ApproveSyncRecordDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-05-12
    * @param dto
    * @return
    */
    Boolean update(ApproveSyncRecordDTO.UpdateDTO dto);


}
