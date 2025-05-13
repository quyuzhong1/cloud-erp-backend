package com.erp.server.workflow.service;
import com.erp.model.workflow.entity.CfgApproveSyncFieldMapEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.workflow.dto.CfgApproveSyncFieldMapDTO;

import java.util.List;

/**
 * <p>
 * ERP审批同步-推送信息配置 服务类
 * </p>
 *
 * @author jack
 * @since 2025-05-12
 */
public interface CfgApproveSyncFieldMapService extends SuperService<CfgApproveSyncFieldMapEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-05-12
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgApproveSyncFieldMapDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-05-12
    * @param dto
    * @return
    */
    Boolean update(CfgApproveSyncFieldMapDTO.UpdateDTO dto);


    List<CfgApproveSyncFieldMapEntity> listByMainIds(List<String> list);
}
