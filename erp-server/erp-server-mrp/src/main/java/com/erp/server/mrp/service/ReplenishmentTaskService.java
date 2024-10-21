package com.erp.server.mrp.service;

import com.erp.model.mrp.entity.ReplenishmentTaskEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * 补货建议同步任务表 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-10-16
 */
public interface ReplenishmentTaskService extends SuperService<ReplenishmentTaskEntity> {

    /**
     * 保存任务
     *
     * @param suggestionIds 建议id
     */
    void saveTask(List<String> suggestionIds);

    /**
     * 根据建议id查询待更新的数据
     *
     * @param suggestionIds 建议id
     */
    List<String> listByWaitAndReplenishment(List<String> suggestionIds);

    /**
     * 编辑状态
     * @param suggestionId 建议id
     * @param code         状态
     */
    void updateStatus(String suggestionId, String code);

    /**
     * 编辑状态
     * @param suggestionId 建议id
     * @param code         状态
     * @param msg          信息
     */
    void updateStatus(String suggestionId, String code, String msg);
}
