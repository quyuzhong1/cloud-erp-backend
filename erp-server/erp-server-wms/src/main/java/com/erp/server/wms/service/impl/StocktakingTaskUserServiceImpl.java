package com.erp.server.wms.service.impl;

import com.erp.model.wms.entity.StocktakingTaskUserEntity;
import com.erp.server.wms.mapper.StocktakingTaskUserMapper;
import com.erp.server.wms.service.StocktakingTaskUserService;
import com.common.business.service.SuperServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 盘点任务 盘点人表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-07-31
 */
@Service
public class StocktakingTaskUserServiceImpl extends SuperServiceImpl<StocktakingTaskUserMapper, StocktakingTaskUserEntity> implements StocktakingTaskUserService {


    /**
     * 获取盘点人信息
     *
     * @param taskIdList
     * @return java.util.List<com.erp.model.wms.entity.StocktakingTaskUserEntity>
     * @author yl
     * @date 2023-08-08 12:16
     */
    @Override
    public List<StocktakingTaskUserEntity> listBaseByTaskIds(List<String> taskIdList) {
        if (CollectionUtils.isEmpty(taskIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(StocktakingTaskUserEntity::getStocktakingTaskId,taskIdList).list();
    }
}
