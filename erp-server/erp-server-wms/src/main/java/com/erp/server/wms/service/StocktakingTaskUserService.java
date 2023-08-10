package com.erp.server.wms.service;

import com.erp.model.wms.entity.StocktakingTaskUserEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * 盘点任务 盘点人表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-07-31
 */
public interface StocktakingTaskUserService extends SuperService<StocktakingTaskUserEntity> {
    /**
     * 根据任务ids 获取数据
     * @param taskIdList
     * @return
     */
    List<StocktakingTaskUserEntity> listBaseByTaskIds(List<String> taskIdList);
    /**
     * 分配任务盘点人
     * @param taskIdList
     * @return
     */
    Boolean assignUser(List<String> taskIdList, List<String> userIdList);
}
