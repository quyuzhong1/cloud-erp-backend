package com.erp.server.workflow.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.erp.model.workflow.dto.ProcessTaskManagementExtDTO;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.model.workflow.entity.ProcessTaskManagementExtEntity;
import com.erp.model.workflow.enums.CfgApproveSyncSyncPlatformEnum;
import com.erp.server.workflow.mapper.ProcessTaskManagementExtMapper;
import com.erp.server.workflow.service.ProcessTaskManagementExtService;
import com.common.business.service.impl.SuperServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * process_task_management拓展表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-05-12
 */
@Slf4j
@Service
public class ProcessTaskManagementExtServiceImpl extends SuperServiceImpl<ProcessTaskManagementExtMapper, ProcessTaskManagementExtEntity> implements ProcessTaskManagementExtService {


    @Override
    public List<ProcessTaskManagementExtDTO.MessageDTO> listMessageIdByTaskIds(List<String> processTaskManagementIds) {
        if(CollUtil.isEmpty(processTaskManagementIds)){
            return Collections.emptyList();
        }
        return baseMapper.listByProcessTaskManagementIds(processTaskManagementIds);
    }

    @Override
    public List<ProcessTaskManagementEntity> listProcessTaskByTaskIds(List<String> processTaskManagementIds,String processInstanceId) {
        if(CollUtil.isEmpty(processTaskManagementIds) || StringUtils.isBlank(processInstanceId)){
            return Collections.emptyList();
        }
        return baseMapper.listProcessTaskByTaskIds(processTaskManagementIds,processInstanceId);
    }
}
