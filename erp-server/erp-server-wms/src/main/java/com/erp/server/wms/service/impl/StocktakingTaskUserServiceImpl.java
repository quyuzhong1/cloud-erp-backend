package com.erp.server.wms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.FindUserDTO;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.OperateLogDTO;
import com.erp.model.wms.entity.StocktakingTaskEntity;
import com.erp.model.wms.entity.StocktakingTaskUserEntity;
import com.erp.rpc.sys.feign.UserInfoFeign;
import com.erp.server.wms.mapper.StocktakingTaskUserMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.StocktakingTaskUserService;
import com.common.business.service.impl.SuperServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
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
     * @param sourceIdList
     * @return java.util.List<com.erp.model.wms.entity.StocktakingTaskUserEntity>
     * @author yl
     * @date 2023-08-08 12:16
     */
    @Override
    public List<StocktakingTaskUserEntity> listBaseBySourceIdList(List<String> sourceIdList) {
        if (CollectionUtils.isEmpty(sourceIdList)) {
            return Collections.emptyList();
        }
        sourceIdList=sourceIdList.stream().filter(s-> CharSequenceUtil.isNotBlank(s)).collect(Collectors.toList());
        return this.lambdaQuery().in(StocktakingTaskUserEntity::getSourceId, sourceIdList).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean assignUser(StocktakingTaskEntity taskEntity, List<String> userIdList) {
        List<OperateLogDTO.AddModuleOperateLogDTO> operateLogList = new ArrayList<>(1);
        List<FindUserDTO> userList = userInfoFeign.listByUserIds(userIdList);
        List<String> taskIdList = Collections.singletonList(taskEntity.getId());
        String moduleType = ModuleTypeEnum.STOCKTAKING_TASK.getCode();
        //第一步先删除
        this.removeBySourceIdList(taskIdList);
        List<StocktakingTaskUserEntity> addList = new ArrayList<>(10);
        String taskId = taskEntity.getId();
        String code = taskEntity.getCode();
        List<String> userNameList = new ArrayList<>();
        //盘点任务
        String stocktakingTask = SourceTypeEnum.STOCKTAKING_TASK.getCode();
        for (String userId : userIdList) {
            StocktakingTaskUserEntity taskUserEntity = new StocktakingTaskUserEntity();
            taskUserEntity.setSourceId(taskId);
            taskUserEntity.setSourceType(stocktakingTask);
            taskUserEntity.setUserId(userId);
            String userName = userList.stream().filter(u -> u.getUserId().equals(userId)).
                    map(FindUserDTO::getUserName).findFirst().orElse("");
            taskUserEntity.setUserName(userName);
            addList.add(taskUserEntity);
            userNameList.add(userName);


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

    /**
     * 添加盘点人
     *
     * @param sourceId
     * @param sourceType
     * @param stocktakingUserIdList
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-10-20 10:02
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addTaskUser(String sourceId, String sourceType, List<String> stocktakingUserIdList) {
        //第一步先删除
        this.removeBySourceIdList(Collections.singletonList(sourceId));
        List<StocktakingTaskUserEntity> addList = new ArrayList<>(10);
        for (String userId : stocktakingUserIdList) {
            StocktakingTaskUserEntity taskUserEntity = new StocktakingTaskUserEntity();
            taskUserEntity.setSourceId(sourceId);
            taskUserEntity.setSourceType(sourceType);
            taskUserEntity.setUserId(userId);
            addList.add(taskUserEntity);
        }
        if (CollectionUtils.isNotEmpty(addList)) {
            return this.saveBatch(addList);
        }
        return Boolean.FALSE;
    }

    @Override
    public void removeBySourceId(String sourceId) {
        this.removeBySourceIdList(Collections.singletonList(sourceId));
    }

    public void removeBySourceIdList(List<String> sourceIdList) {
        if (CollectionUtils.isNotEmpty(sourceIdList)) {
            LambdaQueryWrapper<StocktakingTaskUserEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(StocktakingTaskUserEntity::getSourceId, sourceIdList);
            this.remove(queryWrapper);
        }
    }
}
