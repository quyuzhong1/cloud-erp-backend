package com.erp.server.workflow.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.common.business.dto.FindUserDTO;
import com.erp.model.workflow.entity.ProcessTaskCcEntity;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.model.workflow.enums.CcStatusEnum;
import com.erp.server.workflow.mapper.ProcessTaskCcMapper;
import com.erp.server.workflow.service.ProcessTaskCcService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-05-17
 */
@Service
public class ProcessTaskCcServiceImpl extends SuperServiceImpl<ProcessTaskCcMapper, ProcessTaskCcEntity> implements ProcessTaskCcService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveCcUser(String taskId, List<FindUserDTO> copyUser, String taskManagementId) {
        if(CollectionUtil.isEmpty(copyUser)){
            return;
        }
        for (FindUserDTO user : copyUser) {
            ProcessTaskCcEntity processTaskCcEntity = new ProcessTaskCcEntity();
            processTaskCcEntity.setTaskId(taskId);
            processTaskCcEntity.setCcUserId(user.getUserId());
            processTaskCcEntity.setCcUserName(user.getUserName());
            processTaskCcEntity.setTaskManagementId(taskManagementId);
            this.save(processTaskCcEntity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCcStatus(String processInstanceId, String taskId, String taskManagementId) {
        lambdaUpdate()
                .eq(ProcessTaskCcEntity::getTaskId, taskId)
                .eq(ProcessTaskCcEntity::getTaskManagementId, taskManagementId)
                .set(ProcessTaskCcEntity::getStatus, CcStatusEnum.SEND)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCcTransfer(List<ProcessTaskManagementEntity> entityList, ProcessTaskManagementEntity insertEntity) {
        if(CollectionUtil.isEmpty(entityList)){
            return;
        }
        ConcurrentHashMap<String, String> ccUserMap = new ConcurrentHashMap<>();
        // 更新抄送人 抄送人的任务id改为新的任务id
        lambdaQuery().in(ProcessTaskCcEntity::getTaskManagementId, entityList.stream().map(ProcessTaskManagementEntity::getId).toArray())
                .list().forEach(processTaskCcEntity -> {
            // 抄送人出现第二次则跳过更新
            if(ccUserMap.containsKey(processTaskCcEntity.getCcUserId())){
                return;
            }else {
                ccUserMap.put(processTaskCcEntity.getCcUserId(), processTaskCcEntity.getCcUserId());
            }
            processTaskCcEntity.setTaskManagementId(insertEntity.getId());
            this.updateById(processTaskCcEntity);
        });
    }
}
