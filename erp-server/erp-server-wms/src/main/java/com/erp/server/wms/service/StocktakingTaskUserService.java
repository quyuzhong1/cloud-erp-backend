package com.erp.server.wms.service;

import com.erp.model.wms.entity.StocktakingTaskEntity;
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
    List<StocktakingTaskUserEntity> listBaseBySourceIdList(List<String> taskIdList);
    /**
     * 分配任务盘点人
     * @param taskEntity
     * @return
     */
    Boolean assignUser(StocktakingTaskEntity taskEntity, List<String> userIdList);

    /**
     * 根据用户获取多信息
     * @author yl
     * @date 2023-08-11 9:29
     * @param userIdList
     * @return java.util.List<com.erp.model.wms.entity.StocktakingTaskUserEntity>
     */
    List<StocktakingTaskUserEntity> listByUserIds(List<String> userIdList);

    /**
     * 添加盘点人
     * @author yl
     * @date 2023-10-20 10:02
     * @param sourceId 来源id
     * @param sourceType 来源类型
     * @param stocktakingUserIdList
     * @return java.lang.Boolean
     */
    Boolean addTaskUser(String sourceId,String sourceType,List<String> stocktakingUserIdList);

    /**
     * 删除盘点人根据来源id
     * @author yl
     * @date 2023-10-20 14:25
     * @param sourceId
     * @return void
     */
    void removeBySourceId(String sourceId);
}
