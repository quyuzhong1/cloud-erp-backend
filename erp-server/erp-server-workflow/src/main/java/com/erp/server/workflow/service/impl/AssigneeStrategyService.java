package com.erp.server.workflow.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.erp.model.workflow.dto.CamundaDTO;
import org.jvnet.hk2.annotations.Service;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
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
        List<String> candidateUserList = StrUtil.isNotBlank(candidateUsers) ? Arrays.asList(candidateUsers.split(",")) : Collections.EMPTY_LIST;
        if(null == result){
            return candidateUserList;
        }
        List<String> assignees = result.apply(new CamundaDTO.StrategyParamDTO(value, startUserId));
        if(CollectionUtil.isEmpty(assignees)){
            return candidateUserList;
        }
        return assignees;
    }


}
