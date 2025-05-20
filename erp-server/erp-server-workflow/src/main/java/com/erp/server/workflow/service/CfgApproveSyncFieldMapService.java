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

    List<CfgApproveSyncFieldMapEntity> listByMainIds(List<String> list);
}
