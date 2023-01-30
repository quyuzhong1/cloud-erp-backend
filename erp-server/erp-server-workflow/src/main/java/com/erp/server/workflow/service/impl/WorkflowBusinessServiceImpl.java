package com.erp.server.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.workflow.dto.FindProcessDTO;
import com.erp.model.workflow.dto.WorkflowBusinessDTO;
import com.erp.model.workflow.entity.WorkflowBusinessEntity;
import com.erp.model.workflow.vo.WorkflowBusinessVO;
import com.erp.server.workflow.mapper.WorkflowBusinessMapper;
import com.erp.server.workflow.service.WorkflowBusinessService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Classname WorkflowBusinessServiceImpl
 * @Description TODO
 * @Date 2023-01-30 15:31
 * @Created by yl
 */
@Service
public class WorkflowBusinessServiceImpl extends ServiceImpl<WorkflowBusinessMapper, WorkflowBusinessEntity> implements WorkflowBusinessService {
    @Override
    public List<WorkflowBusinessVO> getBusinessList(FindProcessDTO dto) {
        LambdaQueryWrapper<WorkflowBusinessEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(WorkflowBusinessEntity::getBusinessType, dto.getBusinessType());
        queryWrapper.eq(WorkflowBusinessEntity::getParam, dto.getPlatform());
        List<WorkflowBusinessEntity> list = this.list(queryWrapper);
        List<WorkflowBusinessVO> resultList = new ArrayList<>();
        for (WorkflowBusinessEntity item : list) {
            WorkflowBusinessVO info = new WorkflowBusinessVO();
            info.setId(item.getId());
            info.setBusinessName(item.getBusinessName());
            String param = item.getParam();
            String[] params = param.split(",");
            info.setAuditorTotal(params.length);
            info.setParam(param);
            info.setBusinessKey(item.getBusinessKey());
            resultList.add(info);
        }
        return resultList;
    }


    @Override
    public Boolean saveBusiness(WorkflowBusinessDTO dto) {
        WorkflowBusinessEntity processEntity = new WorkflowBusinessEntity();
        BeanMapper.copy(dto, processEntity);
        return this.save(processEntity);
    }
}
