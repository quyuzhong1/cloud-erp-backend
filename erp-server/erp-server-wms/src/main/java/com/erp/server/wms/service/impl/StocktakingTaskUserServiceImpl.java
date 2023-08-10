package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.FindUserDTO;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.wms.entity.StocktakingTaskUserEntity;
import com.erp.rpc.sys.feign.UserInfoFeign;
import com.erp.server.wms.mapper.StocktakingTaskUserMapper;
import com.erp.server.wms.service.StocktakingTaskUserService;
import com.common.business.service.SuperServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
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


    @Resource
    private UserInfoFeign userInfoFeign;

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
        return this.lambdaQuery().in(StocktakingTaskUserEntity::getStocktakingTaskId, taskIdList).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean assignUser(List<String> taskIdList, List<String> userIdList) {
        List<FindUserDTO> userList = userInfoFeign.listByUserIds(userIdList);
        //第一步先删除
        this.removeByTaskIds(taskIdList);
        List<StocktakingTaskUserEntity> addList = new ArrayList<>(10);
        for (String taskId : taskIdList) {
            for (String userId : userIdList) {
                StocktakingTaskUserEntity taskUserEntity = new StocktakingTaskUserEntity();
                taskUserEntity.setStocktakingTaskId(taskId);
                taskUserEntity.setUserId(userId);
                String userName = userList.stream().filter(u -> u.getUserId().equals(userId)).
                        map(FindUserDTO::getUserName).findFirst().orElse("");
                taskUserEntity.setUserName(userName);
                addList.add(taskUserEntity);
            }

        }
        return this.saveBatch(addList);
    }

    public void removeByTaskIds(List<String> taskIdList) {
        if (CollectionUtils.isNotEmpty(taskIdList)) {
            LambdaQueryWrapper<StocktakingTaskUserEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(StocktakingTaskUserEntity::getStocktakingTaskId, taskIdList);
            this.remove(queryWrapper);
        }
    }
}
