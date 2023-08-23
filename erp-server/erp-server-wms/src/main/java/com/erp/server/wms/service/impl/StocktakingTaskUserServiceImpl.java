package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.FindUserDTO;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.OperateLogDTO;
import com.erp.model.wms.entity.StocktakingTaskEntity;
import com.erp.model.wms.entity.StocktakingTaskUserEntity;
import com.erp.rpc.sys.feign.UserInfoFeign;
import com.erp.server.wms.mapper.StocktakingTaskUserMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.StocktakingTaskUserService;
import com.common.business.service.SuperServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    @Resource
    private OperateLogService operateLogService;

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
    public Boolean assignUser(List<StocktakingTaskEntity> taskEntityList, List<String> userIdList) {
        List<OperateLogDTO.AddModuleOperateLogDTO> operateLogList = new ArrayList<>(taskEntityList.size());

        List<FindUserDTO> userList = userInfoFeign.listByUserIds(userIdList);
        List<String> taskIdList = taskEntityList.stream().map(StocktakingTaskEntity::getId).collect(Collectors.toList());
        String moduleType = ModuleTypeEnum.STOCKTAKING_TASK.getCode();
        //第一步先删除
        this.removeByTaskIds(taskIdList);
        List<StocktakingTaskUserEntity> addList = new ArrayList<>(10);
        for (StocktakingTaskEntity task : taskEntityList) {
            String taskId = task.getId();
            String code = task.getCode();
            List<String> userNameList = new ArrayList<>();
            for (String userId : userIdList) {
                StocktakingTaskUserEntity taskUserEntity = new StocktakingTaskUserEntity();
                taskUserEntity.setStocktakingTaskId(taskId);
                taskUserEntity.setUserId(userId);
                String userName = userList.stream().filter(u -> u.getUserId().equals(userId)).
                        map(FindUserDTO::getUserName).findFirst().orElse("");
                taskUserEntity.setUserName(userName);
                addList.add(taskUserEntity);
                userNameList.add(userName);
            }

            OperateLogDTO.AddModuleOperateLogDTO operateLogDTO = new OperateLogDTO.AddModuleOperateLogDTO();
            operateLogDTO.setOperation("分配盘点人");
            operateLogDTO.setBusinessId(taskId);
            operateLogDTO.setModuleType(moduleType);
            operateLogDTO.setBusinessId(taskId);
            StringBuffer sb = new StringBuffer("盘点任务");
            sb.append(code).append("  分配给").append(userNameList.stream().collect(Collectors.joining(",")));
            operateLogDTO.setContent(sb.toString());
            operateLogList.add(operateLogDTO);
        }
        Boolean result = this.saveBatch(addList);
        operateLogService.batchAddModuleOperateLog(operateLogList);
        return result;
    }


    /**
     * 根据用户获取多信息
     *
     * @param userIdList
     * @return java.util.List<com.erp.model.wms.entity.StocktakingTaskUserEntity>
     * @author yl
     * @date 2023-08-11 9:29
     */
    @Override
    public List<StocktakingTaskUserEntity> listByUserIds(List<String> userIdList) {
        if (CollectionUtils.isEmpty(userIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(StocktakingTaskUserEntity::getUserId, userIdList).list();
    }

    public void removeByTaskIds(List<String> taskIdList) {
        if (CollectionUtils.isNotEmpty(taskIdList)) {
            LambdaQueryWrapper<StocktakingTaskUserEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(StocktakingTaskUserEntity::getStocktakingTaskId, taskIdList);
            this.remove(queryWrapper);
        }
    }
}
