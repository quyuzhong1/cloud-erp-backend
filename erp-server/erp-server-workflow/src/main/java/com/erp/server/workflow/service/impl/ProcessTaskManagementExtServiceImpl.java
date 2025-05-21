package com.erp.server.workflow.service.impl;


import cn.hutool.core.collection.CollUtil;
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
    public List<String> listByProcessTaskManagementIds(List<String> processTaskManagementIds) {
        if(CollUtil.isNotEmpty(processTaskManagementIds)){
            List<ProcessTaskManagementExtEntity> list = lambdaQuery().in(ProcessTaskManagementExtEntity::getProcessTaskManagementId, processTaskManagementIds)
                    .eq(ProcessTaskManagementExtEntity::getSoucePlatform, CfgApproveSyncSyncPlatformEnum.FEISHU.getCode())
                    .list();
            return list.stream().map(ProcessTaskManagementExtEntity::getMessageId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
