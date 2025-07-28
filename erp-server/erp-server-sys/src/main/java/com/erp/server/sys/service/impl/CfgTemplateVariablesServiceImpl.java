package com.erp.server.sys.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.sys.entity.CfgTemplateVariablesEntity;
import com.erp.server.sys.mapper.CfgTemplateVariablesMapper;
import com.erp.server.sys.service.CfgTemplateVariablesService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.sys.service.OperateLogService;
import com.erp.server.sys.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.sys.dto.CfgTemplateVariablesDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
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

    @Override
    public List<CfgTemplateVariablesDTO.VariableGroupDTO> listByTemplateType(CfgTemplateVariablesDTO.TemplateParamDTO dto) {
        if (Objects.isNull(dto) || (StringUtils.isBlank(dto.getTemplateType()) && StringUtils.isBlank(dto.getType()))) {
            return Collections.emptyList();
        }

        List<CfgTemplateVariablesEntity> list = lambdaQuery()
                .eq(CfgTemplateVariablesEntity::getTemplateType, dto.getTemplateType())
                .list();

        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }

        Map<String, CfgTemplateVariablesEntity> parentMap = new HashMap<>();
        Map<String, List<CfgTemplateVariablesEntity>> childGroupMap = new HashMap<>();

        for (CfgTemplateVariablesEntity entity : list) {
            String parentId = entity.getParentId();
            if (StringUtils.equals("0", parentId)) {
                parentMap.put(entity.getId(), entity);
            } else {
                childGroupMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(entity);
            }
        }

        if (parentMap.isEmpty()) {
            return Collections.emptyList();
        }

        List<CfgTemplateVariablesDTO.VariableGroupDTO> result = new ArrayList<>();

        for (CfgTemplateVariablesEntity parent : parentMap.values()) {
            CfgTemplateVariablesDTO.VariableGroupDTO view = new CfgTemplateVariablesDTO.VariableGroupDTO();
            BeanMapper.copy(parent, view);

            List<CfgTemplateVariablesEntity> childList = childGroupMap.getOrDefault(parent.getId(), Collections.emptyList());
            if (CollUtil.isNotEmpty(childList)) {
                List<CfgTemplateVariablesDTO.VariableDTO> variables = BeanMapper.copyList(childList, CfgTemplateVariablesDTO.VariableDTO.class);
                variables.sort(Comparator.comparing(CfgTemplateVariablesDTO.VariableDTO::getIndex));
                view.setVariables(variables);
            }

            result.add(view);
        }

        result.sort(Comparator.comparing(CfgTemplateVariablesDTO.VariableGroupDTO::getIndex));
        return result;
    }


}
