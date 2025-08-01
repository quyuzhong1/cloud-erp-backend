package com.erp.server.sys.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.erp.model.sys.entity.CfgTemplateVariablesEntity;
import com.erp.server.sys.mapper.CfgTemplateVariablesMapper;
import com.erp.server.sys.service.CfgTemplateVariablesService;
import com.common.business.service.impl.SuperServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.sys.dto.CfgTemplateVariablesDTO;
import java.util.*;
import java.util.stream.Collectors;
import com.common.core.utils.*;
/**
 * <p>
 * 模板字段表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-07-24
 */
@Slf4j
@Service
public class CfgTemplateVariablesServiceImpl extends SuperServiceImpl<CfgTemplateVariablesMapper, CfgTemplateVariablesEntity> implements CfgTemplateVariablesService {

//    @Override
//    public List<CfgTemplateVariablesDTO.VariableGroupDTO> listByTemplateType(CfgTemplateVariablesDTO.TemplateParamDTO dto) {
//        if (Objects.isNull(dto) || (StringUtils.isBlank(dto.getTemplateType()) && StringUtils.isBlank(dto.getType()))) {
//            return Collections.emptyList();
//        }
//
//        List<CfgTemplateVariablesEntity> list = lambdaQuery()
//                .eq(CfgTemplateVariablesEntity::getTemplateType, dto.getTemplateType())
//                .list();
//
//        if (CollUtil.isEmpty(list)) {
//            return Collections.emptyList();
//        }
//
//        Map<String, CfgTemplateVariablesEntity> parentMap = new HashMap<>();
//        Map<String, List<CfgTemplateVariablesEntity>> childGroupMap = new HashMap<>();
//
//        for (CfgTemplateVariablesEntity entity : list) {
//            String parentId = entity.getParentId();
//            if (StringUtils.equals("0", parentId)) {
//                parentMap.put(entity.getId(), entity);
//            } else {
//                childGroupMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(entity);
//            }
//        }
//
//        if (parentMap.isEmpty()) {
//            return Collections.emptyList();
//        }
//
//        List<CfgTemplateVariablesDTO.VariableGroupDTO> result = new ArrayList<>();
//
//        for (CfgTemplateVariablesEntity parent : parentMap.values()) {
//            CfgTemplateVariablesDTO.VariableGroupDTO view = new CfgTemplateVariablesDTO.VariableGroupDTO();
//            BeanMapper.copy(parent, view);
//
//            List<CfgTemplateVariablesEntity> childList = childGroupMap.getOrDefault(parent.getId(), Collections.emptyList());
//            if (CollUtil.isNotEmpty(childList)) {
//                List<CfgTemplateVariablesDTO.VariableGroupDTO> variables = BeanMapper.copyList(childList, CfgTemplateVariablesDTO.VariableGroupDTO.class);
//                variables.sort(Comparator.comparing(CfgTemplateVariablesDTO.VariableGroupDTO::getIndex));
//                view.setVariables(variables);
//            }
//            result.add(view);
//        }
//        result.sort(Comparator.comparing(CfgTemplateVariablesDTO.VariableGroupDTO::getIndex));
//        return result;
//    }


    @Override
    public List<CfgTemplateVariablesDTO.VariableGroupDTO> listByTemplateType(CfgTemplateVariablesDTO.TemplateParamDTO dto) {
        if (Objects.isNull(dto) || (StringUtils.isBlank(dto.getTemplateType()) && StringUtils.isBlank(dto.getType()))) {
            return Collections.emptyList();
        }

        List<CfgTemplateVariablesEntity> list = lambdaQuery()
                .eq(CfgTemplateVariablesEntity::getTemplateType, dto.getTemplateType())
                .eq(CfgTemplateVariablesEntity::getType, dto.getType())
                .eq(CfgTemplateVariablesEntity::getType, dto.getType())
                .eq(CfgTemplateVariablesEntity::getDisabled, false)
                .list();

        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }

        // 构建ID到实体的映射，便于快速查找
        Map<String, CfgTemplateVariablesEntity> entityMap = list.stream()
                .collect(Collectors.toMap(CfgTemplateVariablesEntity::getId, entity -> entity));

        // 构建树形结构
        List<CfgTemplateVariablesDTO.VariableGroupDTO> result = new ArrayList<>();

        for (CfgTemplateVariablesEntity entity : list) {
            // 如果是根节点，则创建 VariableGroupDTO
            if (StringUtils.equals("0", entity.getParentId())) {
                CfgTemplateVariablesDTO.VariableGroupDTO group = new CfgTemplateVariablesDTO.VariableGroupDTO();
                BeanMapper.copy(entity, group);
                // 递归构建子节点
                buildChildren(group, entityMap, list);
                result.add(group);
            }
        }

        // 按索引排序
        result.sort(Comparator.comparing(CfgTemplateVariablesDTO.VariableGroupDTO::getIndex));
        return result;
    }

    /**
     * 递归构建子节点树
     */
    private void buildChildren(CfgTemplateVariablesDTO.VariableGroupDTO parent,
                               Map<String, CfgTemplateVariablesEntity> entityMap,
                               List<CfgTemplateVariablesEntity> allEntities) {
        List<CfgTemplateVariablesDTO.VariableGroupDTO> children = new ArrayList<>();

        for (CfgTemplateVariablesEntity entity : allEntities) {
            if (StringUtils.equals(parent.getId(), entity.getParentId())) {
                CfgTemplateVariablesDTO.VariableGroupDTO variable = new CfgTemplateVariablesDTO.VariableGroupDTO();
                BeanMapper.copy(entity, variable);
                children.add(variable);

                // 如果该节点还有子节点，继续递归处理
                buildChildren(variable, entityMap, allEntities);
            }
        }

        if (!children.isEmpty()) {
            children.sort(Comparator.comparing(CfgTemplateVariablesDTO.VariableGroupDTO::getIndex));
            parent.setVariables(children);
        }
    }

}
