package com.erp.server.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.workflow.dto.BusinessTableDTO;
import com.erp.model.workflow.dto.WorkflowBusinessProcessDTO;
import com.erp.model.workflow.entity.WorkflowBusinessProcessEntity;
import com.erp.model.workflow.vo.MyToDoTaskVO;
import com.erp.model.workflow.vo.ProcessCurrentAuditorVO;
import com.erp.server.workflow.mapper.WorkflowBusinessProcessMapper;
import com.erp.server.workflow.service.ProcessTaskService;
import com.erp.server.workflow.service.WorkflowBusinessProcessService;
import org.apache.commons.collections4.CollectionUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Classname WorkflowBusinessProcessServiceImpl
 * @Description TODO
 * @Date 2023-01-30 15:35
 * @Created by yl
 */
@Service
public class WorkflowBusinessProcessServiceImpl extends ServiceImpl<WorkflowBusinessProcessMapper, WorkflowBusinessProcessEntity> implements WorkflowBusinessProcessService {


    @Autowired
    private ProcessTaskService processTaskService;

    @Autowired
    private HistoryService historyService;


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

    @Override
    public List<WorkflowBusinessProcessEntity> getByProcessIds(List<String> processIds) {
        if (CollectionUtils.isNotEmpty(processIds)) {
            LambdaQueryWrapper<WorkflowBusinessProcessEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(WorkflowBusinessProcessEntity::getProcessId, processIds);
            return this.list(queryWrapper);
        }
        return new ArrayList<>();
    }


    /**
     * 保存 业务与流程的关系
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-01-31 17:20
     */
    @Override
    public Boolean saveBusinessProcess(WorkflowBusinessProcessDTO dto) {
        if (dto != null) {
            WorkflowBusinessProcessEntity process = new WorkflowBusinessProcessEntity();
            BeanMapper.copy(dto, process);
            return this.save(process);
        }
        return false;
    }


    /**
     * 根据业务表 和用户获取到
     *
     * @param dto
     * @return com.erp.model.workflow.vo.MyToDoTaskVO
     * @author yl
     * @date 2023-02-01 9:25
     */
    @Override
    public MyToDoTaskVO getProcessByBusinessTable(BusinessTableDTO dto) {
        //获取到我的待办任务
        List<MyToDoTaskVO> list = processTaskService.getMyToDoTasks(dto.getUserId());
        MyToDoTaskVO taskVO = list.stream().
                filter(m -> StringUtils.isNotBlank(m.getBusinessTableId()) && m.getBusinessTableId().
                        equals(dto.getBusinessTableId())).findFirst().orElse(null);
        return taskVO;
    }


    /**
     * 获取到 当前审核人
     *
     * @param businessTableIds
     * @return java.util.List<com.erp.model.workflow.vo.ProcessCurrentAuditorVO>
     * @author yl
     * @date 2023-02-09 9:23
     */
    @Override
    public List<ProcessCurrentAuditorVO> getProcessCurrentAuditor(List<String> businessTableIds) {
        LambdaQueryWrapper<WorkflowBusinessProcessEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(WorkflowBusinessProcessEntity::getBusinessTableId, businessTableIds);
        List<WorkflowBusinessProcessEntity> list = this.list(queryWrapper);
        List<ProcessCurrentAuditorVO> resultList = new ArrayList<>(list.size());
        if (CollectionUtils.isNotEmpty(list)) {
            for (WorkflowBusinessProcessEntity item : list) {
                ProcessCurrentAuditorVO vo = new ProcessCurrentAuditorVO();
                vo.setBusinessTableId(item.getBusinessTableId());
                String processId = item.getProcessId();
                vo.setProcessId(processId);

                List<HistoricTaskInstance> historyList = historyService // 历史相关Service
                        .createHistoricTaskInstanceQuery() // 创建历史任务实例查询
                        .processInstanceId(processId) // 用流程实例id查询
                        .orderByHistoricActivityInstanceStartTime()
                        .asc()
                        .list();
                List<String> assigneeList = historyList.stream().filter(h -> h.getEndTime() == null).map(HistoricTaskInstance::getAssignee).collect(Collectors.toList());
                vo.setHandleUserIdList(assigneeList);
                resultList.add(vo);
            }

        }

        return  resultList;

    }


    /**
     * 根据业务表id 获取到对应 流程信息
     *
     * @param businessTableIds
     * @return java.util.List<com.erp.model.workflow.dto.WorkflowBusinessProcessDTO>
     * @author yl
     * @date 2023-02-11 11:52
     */
    @Override
    public List<WorkflowBusinessProcessDTO> getProcessByTables(List<String> businessTableIds) {
        if (CollectionUtils.isNotEmpty(businessTableIds)) {
            LambdaQueryWrapper<WorkflowBusinessProcessEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(WorkflowBusinessProcessEntity::getBusinessTableId, businessTableIds);
            queryWrapper.orderByDesc(WorkflowBusinessProcessEntity::getCreateTime);
            List<WorkflowBusinessProcessEntity> list = this.list(queryWrapper);
            List<WorkflowBusinessProcessDTO> resultList = new ArrayList<>();
            resultList = BeanMapper.copyList(list, WorkflowBusinessProcessDTO.class);
            return resultList;
        }
        return new ArrayList<>();

    }
}
