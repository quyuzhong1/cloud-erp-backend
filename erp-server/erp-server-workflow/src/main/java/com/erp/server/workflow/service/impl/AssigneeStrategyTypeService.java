package com.erp.server.workflow.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.FindUserDTO;
import com.erp.model.sys.dto.SysFeignDTO;
import com.erp.model.sys.dto.UserSuperiorDTO;
import com.erp.model.sys.enums.ChargeSuperiorEnum;
import com.erp.model.workflow.dto.CamundaDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import org.jvnet.hk2.annotations.Service;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程审批人策略
 *
 * @Author Cloud
 * @Date 2023/4/27 14:11
 **/
@Component
public class AssigneeStrategyTypeService {

    @Resource
    private SysUserFeign sysUserFeign;

    /**
     * 角色审批人
     * @param dto
     * @return
     */
    public List<String> roleAssignee(CamundaDTO.StrategyParamDTO dto) {
        // 根据角色查询用户
        String assignee = dto.getAssignee();
        // 发起人
        String startUserId = dto.getStartUserId();
        if(StrUtil.isNotBlank(assignee)){
            return Arrays.asList(assignee.split(","));
        }
        List<String> roleIds = StrUtil.isNotBlank(assignee) ? Arrays.asList(assignee.split(",")) : Collections.EMPTY_LIST;
        List<FindUserDTO> userList = sysUserFeign.getUserListByRoleIds(new SysFeignDTO.ListByRoleIdsDTO(roleIds, startUserId));
        if(CollectionUtil.isEmpty(userList)){
            return Collections.EMPTY_LIST;
        }
        return userList.stream().map(FindUserDTO::getUserId).collect(Collectors.toList());
    }

    /**
     * 上级审批人
     * @param dto
     * @return
     */
    public List<String> superiorAssignee(CamundaDTO.StrategyParamDTO dto) {
        // 根据用户查询上级
        List<UserSuperiorDTO> superList = sysUserFeign.listSuperiorByUserIds(Arrays.asList(dto.getStartUserId()));
        if(CollectionUtil.isEmpty(superList)){
            return Collections.EMPTY_LIST;
        }
        ChargeSuperiorEnum chargeSuperior = ChargeSuperiorEnum.getByDictValue(dto.getAssignee());
        // 默认直接上级
        chargeSuperior = null == chargeSuperior ? ChargeSuperiorEnum.DIRECT_SUPERIOR : chargeSuperior;
        // 如果所选上级不存在, 则继续向上查找
        ChargeSuperiorEnum finalChargeSuperior = chargeSuperior;
        String userId = superList.stream()
                .sorted(Comparator.comparing(UserSuperiorDTO::getLevel))
                .filter(superior -> superior.getLevel() >= finalChargeSuperior.getCode())
                .findFirst()
                .map(UserSuperiorDTO::getUserId)
                .orElse(null);
        // 发起人
        return null != userId ? Arrays.asList(userId) : Collections.EMPTY_LIST;
    }

    /**
     * 发起人审批人
     * @param dto
     * @return
     */
    public List<String> initiatorAssignee(CamundaDTO.StrategyParamDTO dto) {
        // 发起人转list
        return Arrays.asList(dto.getStartUserId());
    }

    /**
     * 指定人审批人
     * @param dto
     * @return
     */
    public List<String> somebodyAssignee(CamundaDTO.StrategyParamDTO dto) {
        String assignee = dto.getAssignee();
        return  StrUtil.isNotBlank(assignee) ? Arrays.asList(assignee.split(",")) : Collections.EMPTY_LIST;
    }
}
