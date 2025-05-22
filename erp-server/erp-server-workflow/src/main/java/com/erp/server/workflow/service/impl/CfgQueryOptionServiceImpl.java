package com.erp.server.workflow.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.core.enums.RuleCompareEnum;
import com.erp.model.workflow.dto.CfgQueryOptionDTO;
import com.erp.model.workflow.entity.CfgQueryOptionEntity;
import com.erp.model.workflow.enums.CfgQueryOptionFieldTypeEnum;
import com.erp.server.workflow.mapper.CfgQueryOptionMapper;
import com.erp.server.workflow.service.CfgQueryOptionService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * <p>
 * 查询option配置表(数大臣单据字段) 服务实现类
 * </p>
 *
 * @author hcg
 * @since 2025-05-15
 */
@Slf4j
@Service
public class CfgQueryOptionServiceImpl extends SuperServiceImpl<CfgQueryOptionMapper, CfgQueryOptionEntity> implements CfgQueryOptionService {


    @Override
    public List<CfgQueryOptionDTO.ListDTO> proDropDown(String bussinessKey) {
        return baseMapper.proDropDown(bussinessKey);
    }


    @Override
    public List<CfgQueryOptionDTO.cfgApproveSyncDropDownDTO> cfgApproveSyncDropDown(String bussinessKey) {
        List<CfgQueryOptionEntity> cfgQueryOptionEntities = lambdaQuery().eq(CfgQueryOptionEntity::getBussinessKey, bussinessKey).orderByDesc(CfgQueryOptionEntity::getFieldBelongsType).list();
        return BeanUtil.copyToList(cfgQueryOptionEntities, CfgQueryOptionDTO.cfgApproveSyncDropDownDTO.class);
    }

    @Override
    public List<CfgQueryOptionDTO.TreeDTO> tree(String bussinessKey) {
        List<CfgQueryOptionEntity> cfgQueryOptionEntities = this.list(new LambdaQueryWrapper<CfgQueryOptionEntity>().eq(CfgQueryOptionEntity::getBussinessKey, bussinessKey).eq(CfgQueryOptionEntity::getIsDeleted, false));
        List<CfgQueryOptionDTO.TreeDTO> resultList = new ArrayList<>(cfgQueryOptionEntities.size());
        Map<String, String> map = new HashMap<>();
        for (RuleCompareEnum item : RuleCompareEnum.values()) {
            map.put(item.getCode(), item.getName());
        }
        for (CfgQueryOptionEntity item : cfgQueryOptionEntities) {
            String conditionField = item.getConditionField();
            CfgQueryOptionDTO.TreeDTO tree = new CfgQueryOptionDTO.TreeDTO();
            tree.setConditionField(conditionField);
            String logicStr = item.getLogic();
            List<String> logicList = Arrays.asList(logicStr.split(","));
            List<CfgQueryOptionDTO.TreeDTO> childrenList = new ArrayList<>(logicList.size());
            for (String logic : logicList) {
                CfgQueryOptionDTO.TreeDTO children = new CfgQueryOptionDTO.TreeDTO();
                children.setConditionField(conditionField);
                children.setLogic(logic);
                children.setLogicName(map.getOrDefault(logic, ""));
                childrenList.add(children);
            }
            tree.setChildren(childrenList);
            resultList.add(tree);
        }
        return resultList;
    }

    @Override
    public List<CfgQueryOptionDTO.ViewDTO> getSystemfield(String bussinessKey) {
        List<CfgQueryOptionEntity> cfgQueryOptionEntities = baseMapper.getSystemfield(bussinessKey);
        List<CfgQueryOptionDTO.ViewDTO> viewDTOS = BeanUtil.copyToList(cfgQueryOptionEntities, CfgQueryOptionDTO.ViewDTO.class);
        viewDTOS.forEach(item -> {
            if (StrUtil.isNotBlank(item.getFieldType())) {
                item.setFieldTypeName(CfgQueryOptionFieldTypeEnum.valueOf(item.getFieldType().toUpperCase()).getName());
            }
        });
        return viewDTOS;
    }
}
