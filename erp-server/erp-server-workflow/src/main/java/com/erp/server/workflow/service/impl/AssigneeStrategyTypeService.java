package com.erp.server.workflow.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.dto.FindUserDTO;
import com.erp.model.sys.dto.SysFeignDTO;
import com.erp.model.sys.dto.UserSuperiorDTO;
import com.erp.model.sys.enums.ChargeSuperiorEnum;
import com.erp.model.workflow.dto.CamundaDTO;
import com.erp.model.workflow.enums.DictBasicEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import org.apache.commons.collections4.CollectionUtils;
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
        String assignee = dto.getPropertiesDTO().getAssignee();
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
        ChargeSuperiorEnum chargeSuperior = ChargeSuperiorEnum.getByDictValue(dto.getPropertiesDTO().getAssignee());
        // 默认直接上级
        chargeSuperior = null == chargeSuperior ? ChargeSuperiorEnum.DIRECT_SUPERIOR : chargeSuperior;
        String userId = findSuperiorByLevelSequence(superList, chargeSuperior.getCode());
        // 发起人
        return null != userId ? Arrays.asList(userId) : Collections.emptyList();
    }


    /**
     * 逐级查找上级
     * @author will
     * @date 2025/10/27 10:40
     * @param superList
     * @param startLevel
     * @return String
     */
    private String findSuperiorByLevelSequence(List<UserSuperiorDTO> superList, int startLevel) {
        // 获取系统中所有可能的级别
        List<Integer> allSystemLevels = Arrays.stream(ChargeSuperiorEnum.values()).map(ChargeSuperiorEnum::getCode).collect(Collectors.toList()); 

        // 从起始级别开始，按顺序查找
        for (int level : allSystemLevels) {
            if (level >= startLevel) {
                Optional<UserSuperiorDTO> superior = superList.stream()
                        .filter(s -> s.getLevel() == level)
                        .findFirst();
                if (superior.isPresent()) {
                    return superior.get().getUserId();
                }
            }
        }

        // 如果都找不到，返回最高级别的上级
        return superList.stream()
                .max(Comparator.comparing(UserSuperiorDTO::getLevel))
                .map(UserSuperiorDTO::getUserId)
                .orElse(null);
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
     * 指定人审批
     * @param dto
     * @return
     */
    public List<String> somebodyAssignee(CamundaDTO.StrategyParamDTO dto) {
        String assignee = dto.getPropertiesDTO().getAssignee();
        return  CharSequenceUtil.isNotBlank(assignee) ? Arrays.asList(assignee.split(",")) : Collections.emptyList();
    }

    /**
     * 指定人-表达式审批
     * @param dto 参数
     * @return 审批人
     */
    public List<String> somebodyExpAssignee(CamundaDTO.StrategyParamDTO dto) {
        String assignee = dto.getPropertiesDTO().getSomebody_exp();
        List<String> expStrList = CharSequenceUtil.isNotBlank(assignee) ? Arrays.asList(assignee.split(",")) : Collections.emptyList();
        return ProcessManagementServiceImpl.replaceApproveVariables(expStrList, dto.getVariablesMap());
    }
    /**
     * 指定角色
     * @param value 参数
     * @return 审批人
     */

    public List<String> designatedRoleAssignee(CamundaDTO.StrategyParamDTO value) {
        //根据类型判断
        //现在只有SKU产品经理的查找逻辑
        DictBasicEnum dictBasicEnum = DictBasicEnum.getByCode(value.getPropertiesDTO().getAssignee());
        if (dictBasicEnum == null) {
            return Collections.emptyList();
        }
        // 这里只处理SKU产品经理
        if (DictBasicEnum.PRODUCT_MANAGER.equals(dictBasicEnum)) {
            // 从变量map中获取skuProductManagerId
            Map<String, Object> variablesMap = value.getVariablesMap();
            String skuProductManagerId = (String) variablesMap.getOrDefault(DictBasicEnum.PRODUCT_MANAGER.getCode(), "");
            return CharSequenceUtil.isNotBlank(skuProductManagerId) ? Arrays.asList(skuProductManagerId.split(",")) : Collections.emptyList();
        }
        // 其他角色类型可根据需要补充
        return Collections.emptyList();
    }
    /**
     * DQE负责人
     * @param value 参数
     * @return 审批人
     */
    public List<String> dqeOwnerAssignee(CamundaDTO.StrategyParamDTO value) {
        //获取DQE负责人
        //从参数map里面获取
        Map<String, Object> variablesMap = value.getVariablesMap();
        String dqeOwnerId = (String) variablesMap.getOrDefault("dqeOwnerId","");
        return   CharSequenceUtil.isNotBlank(dqeOwnerId) ? Arrays.asList(dqeOwnerId.split(",")) : Collections.emptyList();
    }
}
