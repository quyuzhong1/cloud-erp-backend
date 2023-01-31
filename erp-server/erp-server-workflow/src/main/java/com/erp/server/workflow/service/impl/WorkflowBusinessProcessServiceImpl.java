package com.erp.server.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.workflow.entity.WorkflowBusinessProcessEntity;
import com.erp.server.workflow.mapper.WorkflowBusinessProcessMapper;
import com.erp.server.workflow.service.WorkflowBusinessProcessService;
import org.springframework.stereotype.Service;

/**
 * @Classname WorkflowBusinessProcessServiceImpl
 * @Description TODO
 * @Date 2023-01-30 15:35
 * @Created by yl
 */
@Service
public class WorkflowBusinessProcessServiceImpl extends ServiceImpl<WorkflowBusinessProcessMapper, WorkflowBusinessProcessEntity> implements WorkflowBusinessProcessService {

    /**
     * 根据流程id 获取到业务数据
     *
     * @param processId
     * @return com.erp.model.workflow.entity.WorkflowBusinessProcessEntity
     * @author yl
     * @date 2023-01-30 18:43
     */
    @Override
    public WorkflowBusinessProcessEntity getByProcessId(String processId) {
        LambdaQueryWrapper<WorkflowBusinessProcessEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WorkflowBusinessProcessEntity::getProcessId, processId);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);
    }
}
