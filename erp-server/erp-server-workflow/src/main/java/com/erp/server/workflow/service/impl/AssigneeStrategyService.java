package com.erp.server.workflow.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import com.erp.model.workflow.dto.CamundaDTO;
import com.erp.model.workflow.enums.DictBasicEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;

/**
 * 流程审批人策略
 *
 * @Author Cloud
 * @Date 2023/4/27 14:11
 **/
@Component
public class AssigneeStrategyService {
    @Resource
    private AssigneeStrategyTypeService assigneeStrategyTypeService;


    private Map<String, Function<CamundaDTO.StrategyParamDTO, List<String>>> assigneeStrategyMap = new HashMap<>();

    /**
     * 初始化业务分派逻辑 代替switch
     * key:业务类型
     * value:业务分派逻辑
     */
    @PostConstruct
    public void init() {
        // 添加审批选项
        // 角色
        assigneeStrategyMap.put("role", value -> assigneeStrategyTypeService.roleAssignee(value));
        //上级
        assigneeStrategyMap.put("superior", value -> assigneeStrategyTypeService.superiorAssignee(value));
        //初始化默认值
        assigneeStrategyMap.put("initiator", value -> assigneeStrategyTypeService.initiatorAssignee(value));
        //指定人
        assigneeStrategyMap.put("somebody", value -> assigneeStrategyTypeService.somebodyAssignee(value));
        //指定人-表达式
        assigneeStrategyMap.put("somebody_exp", value -> assigneeStrategyTypeService.somebodyExpAssignee(value));
    }
    public List<String> getResult(CamundaDTO.PropertiesDTO propertiesDTO, String startUserId, String candidateUsers, Map<String, Object> variables) {
        //Controller根据 类型resourceType、编码resourceId 去查询 发放方式grantType
        Function<CamundaDTO.StrategyParamDTO,List<String>> result = assigneeStrategyMap.get(propertiesDTO.getAssigneeOption());
        List<String> candidateUserList = CharSequenceUtil.isNotBlank(candidateUsers) ? Arrays.asList(candidateUsers.split(",")) : Collections.emptyList();
        if(null == result){
            return candidateUserList;
        }
        List<String> assignees = result.apply(new CamundaDTO.StrategyParamDTO(propertiesDTO, startUserId, variables));
        if(CollectionUtils.isEmpty(assignees)){
            return candidateUserList;
        }
        return assignees;
    }

    /**
     * 无审批人处理
     *
     * @param assigneeEmpty 审批为空处理方式
     * @param startUserId
     * @return 审批人
     */
    public List<String> assigneeEmptyHandler(String assigneeEmpty, String startUserId) {
        // 审批为空处理方式为空
        if(CharSequenceUtil.isEmpty(assigneeEmpty)){
            return Collections.emptyList();
        }
        if(DictBasicEnum.REJECT_APPLICANT.getCode().equals(assigneeEmpty)) {
            // 审批为空处理方式驳回审批人 返回空审核人列表，在层方法处理驳回操作
            return Collections.emptyList();
        } else if(DictBasicEnum.ESCALATE.getCode().equals(assigneeEmpty)){
            // 审批为空处理方式转上级审批
            return assigneeStrategyTypeService.superiorAssignee(new CamundaDTO.StrategyParamDTO(startUserId));
        }
        return Collections.emptyList();

    }


}
