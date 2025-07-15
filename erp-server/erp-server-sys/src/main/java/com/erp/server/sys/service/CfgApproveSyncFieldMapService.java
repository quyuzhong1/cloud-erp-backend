package com.erp.server.sys.service;
import com.erp.model.sys.entity.CfgApproveSyncFieldMapEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * ERP审批同步-推送信息配置 服务类
 * </p>
 *
 * @author jack
 * @since 2025-05-23
 */
public interface CfgApproveSyncFieldMapService extends SuperService<CfgApproveSyncFieldMapEntity> {

    List<CfgApproveSyncFieldMapEntity> listByMainIds(List<String> list);
}
