package com.erp.server.workflow.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.common.business.dto.FindUserDTO;
import com.erp.model.workflow.entity.ProcessTaskCcEntity;
import com.erp.server.workflow.mapper.ProcessTaskCcMapper;
import com.erp.server.workflow.service.ProcessTaskCcService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public void saveCcUser(String taskId, List<FindUserDTO> copyUser) {
        if(CollectionUtil.isEmpty(copyUser)){
            return;
        }
        for (FindUserDTO user : copyUser) {
            ProcessTaskCcEntity processTaskCcEntity = new ProcessTaskCcEntity();
            processTaskCcEntity.setTaskId(taskId);
            processTaskCcEntity.setCcUserId(user.getUserId());
            processTaskCcEntity.setCcUserName(user.getUserName());
            this.save(processTaskCcEntity);
        }
    }
}
