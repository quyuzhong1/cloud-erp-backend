package com.erp.server.workflow.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.dto.FindUserDTO;
import com.erp.model.sys.dto.SysFeignDTO;
import com.erp.model.sys.dto.UserSuperiorDTO;
import com.erp.model.sys.enums.ChargeSuperiorEnum;
import com.erp.model.workflow.dto.CamundaDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
        if(CharSequenceUtil.isBlank(assignee)){
            return Collections.emptyList();
        }
        List<String> roleIds = CharSequenceUtil.isNotBlank(assignee) ? Arrays.asList(assignee.split(",")) : Collections.emptyList();
        List<FindUserDTO> userList = sysUserFeign.getUserListByRoleIds(new SysFeignDTO.ListByRoleIdsDTO(roleIds, startUserId));
        if(CollectionUtils.isEmpty(userList)){
            return Collections.emptyList();
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
        if(CollectionUtils.isEmpty(superList)){
            return Collections.emptyList();
        }
        ChargeSuperiorEnum chargeSuperior = ChargeSuperiorEnum.getByDictValue(dto.getAssignee());
        // 默认直接上级
        chargeSuperior = null == chargeSuperior ? ChargeSuperiorEnum.DIRECT_SUPERIOR : chargeSuperior;
        // 如果所选上级不存在, 则继续向上查找
        ChargeSuperiorEnum finalChargeSuperior = chargeSuperior;
        String userId = superList.stream()
                .filter(superior -> superior.getLevel() >= finalChargeSuperior.getCode())
                .min(Comparator.comparing(UserSuperiorDTO::getLevel))
                .map(UserSuperiorDTO::getUserId)
                // 如果所选上级不存在,取最高级别的上级
                .orElseGet(() ->
                    superList.stream()
                    .max(Comparator.comparing(UserSuperiorDTO::getLevel))
                    .map(UserSuperiorDTO::getUserId)
                    .orElse(null)
                );
        // 发起人
        return null != userId ? Arrays.asList(userId) : Collections.emptyList();
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
        return  CharSequenceUtil.isNotBlank(assignee) ? Arrays.asList(assignee.split(",")) : Collections.emptyList();
    }
}
