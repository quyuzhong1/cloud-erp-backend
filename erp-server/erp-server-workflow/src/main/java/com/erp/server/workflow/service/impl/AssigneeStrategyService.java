package com.erp.server.workflow.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import com.erp.model.workflow.dto.CamundaDTO;
import com.erp.model.workflow.enums.DictBasicEnum;
import org.apache.commons.collections4.CollectionUtils;
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
        // role
        assigneeStrategyMap.put("role", value -> assigneeStrategyTypeService.roleAssignee(value));
        //superior
        assigneeStrategyMap.put("superior", value -> assigneeStrategyTypeService.superiorAssignee(value));
        //initiator
        assigneeStrategyMap.put("initiator", value -> assigneeStrategyTypeService.initiatorAssignee(value));
        //somebody
        assigneeStrategyMap.put("somebody", value -> assigneeStrategyTypeService.somebodyAssignee(value));
    }
    public List<String> getResult(String resourceType, String value, String startUserId, String candidateUsers) {
        //Controller根据 优惠券类型resourceType、编码resourceId 去查询 发放方式grantType
        Function<CamundaDTO.StrategyParamDTO,List<String>> result = assigneeStrategyMap.get(resourceType);
        List<String> candidateUserList = CharSequenceUtil.isNotBlank(candidateUsers) ? Arrays.asList(candidateUsers.split(",")) : Collections.emptyList();
        if(null == result){
            return candidateUserList;
        }
        List<String> assignees = result.apply(new CamundaDTO.StrategyParamDTO(value, startUserId));
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
