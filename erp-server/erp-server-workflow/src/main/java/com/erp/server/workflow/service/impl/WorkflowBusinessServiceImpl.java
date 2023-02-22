package com.erp.server.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.common.business.dto.FindUserDTO;
import com.erp.model.workflow.dto.AuditorHandleDTO;
import com.erp.model.workflow.dto.BusinessInfoDTO;
import com.erp.model.workflow.dto.FindProcessDTO;
import com.erp.model.workflow.dto.WorkflowBusinessDTO;
import com.erp.model.workflow.entity.WorkflowBusinessEntity;
import com.erp.model.workflow.vo.ApproveNodeRecordVO;
import com.erp.model.workflow.vo.WorkflowBusinessVO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.workflow.mapper.WorkflowBusinessMapper;
import com.erp.server.workflow.service.ProcessTaskService;
import com.erp.server.workflow.service.WorkflowBusinessService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Classname WorkflowBusinessServiceImpl
 * @Description TODO
 * @Date 2023-01-30 15:31
 * @Created by yl
 */
@Service
public class WorkflowBusinessServiceImpl extends ServiceImpl<WorkflowBusinessMapper, WorkflowBusinessEntity> implements WorkflowBusinessService {
     @Resource
     private ProcessTaskService processTaskService;

    @Resource
    private SysUserFeign sysUserFeign;

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

    /**
     * 获取到流程 启动流程 所需要的信息
     *
     * @param dto
     * @return com.erp.model.workflow.dto.BusinessInfoDTO
     * @author yl
     * @date 2023-01-31 15:49
     */
    @Override
    public BusinessInfoDTO getBusiness(FindProcessDTO dto) {
        BusinessInfoDTO result = new BusinessInfoDTO();
        LambdaQueryWrapper<WorkflowBusinessEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(WorkflowBusinessEntity::getBusinessType, dto.getBusinessType());
        queryWrapper.eq(WorkflowBusinessEntity::getPlatform, dto.getPlatform());
        queryWrapper.last("LIMIT 1");
        WorkflowBusinessEntity businessEntity = this.getOne(queryWrapper);
        if (businessEntity != null) {
            result.setId(businessEntity.getId());
            result.setBusinessKey(businessEntity.getBusinessKey());
            result.setBusinessName(businessEntity.getBusinessName());
            result.setProcessDefinitionKey(businessEntity.getProcessDefinitionKey());
            String param = businessEntity.getParam();
            result.setParam(param);
            if (StringUtils.isNotBlank(param)) {
                List<String> paramList = Arrays.asList(param.split(","));
                result.setParamList(paramList);
            }
            return result;
        }
        return null;

    }

    @Override
    public List<ApproveNodeRecordVO> auditInfo(String processId) {
        List<ApproveNodeRecordVO> resultList = new ArrayList<>();
        List<AuditorHandleDTO> list = processTaskService.getHistoryTaskByProcessId(processId);
        if (CollectionUtils.isEmpty(list)) {
            return resultList;
        }
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        for (AuditorHandleDTO item : list) {
            FindUserDTO findUser = userList.stream().filter(u -> item.getHandleUserId().equals(u.getUserId())).findFirst().orElse(null);
            if (findUser != null) {
                item.setHandleUserName(findUser.getUserName());
            } else {
                item.setHandleUserName("");
            }
        }
        LinkedHashMap<String, List<AuditorHandleDTO>> map = list.stream().
                collect(Collectors.groupingBy(AuditorHandleDTO::getTaskDefinitionKey, LinkedHashMap::new, Collectors.toList()));
        for (Map.Entry<String, List<AuditorHandleDTO>> item : map.entrySet()) {
            ApproveNodeRecordVO vo = new ApproveNodeRecordVO();
            vo.setAuditorHandleList(item.getValue());
            resultList.add(vo);
        }
        return resultList;
    }
}
