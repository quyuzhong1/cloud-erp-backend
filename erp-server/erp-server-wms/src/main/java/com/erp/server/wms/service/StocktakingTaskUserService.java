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

    /**
     * 根据用户获取多信息
     * @author yl
     * @date 2023-08-11 9:29
     * @param asList
     * @return java.util.List<com.erp.model.wms.entity.StocktakingTaskUserEntity>
     */
    List<StocktakingTaskUserEntity> listByUserIds(List<String> asList);
}
