package com.erp.server.tms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.wrapper.FeignQuery;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.entity.DictRuleConditionEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.tms.dto.CfgConditionDTO;
import com.erp.model.tms.entity.CfgConditionEntity;
import com.erp.server.tms.mapper.CfgConditionMapper;
import com.erp.server.tms.service.CfgConditionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 条件配置表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2026-04-22
 */
@Service
public class CfgConditionServiceImpl extends SuperServiceImpl<CfgConditionMapper, CfgConditionEntity> implements CfgConditionService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(CfgConditionDTO.AddDTO dto) {
        CfgConditionEntity cfConditionEntity = new CfgConditionEntity();
        BeanMapperUtils.copy(dto, cfConditionEntity);
        super.save(cfConditionEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(CfgConditionDTO.UpdateDTO dto) {
        CfgConditionEntity cfConditionEntity = BeanMapperUtils.map(CfgConditionEntity.class, dto);
        super.updateById(cfConditionEntity);
    }

    @Override
    public List<CfgConditionDTO.CommonDTO> listByType(String type) {
        return baseMapper.listByType(type);
    }

    private Map<String, String> getDictRuleMap(){
        List<DictRuleConditionEntity> list = FeignQuery.list(FeignQuery.create(DictRuleConditionEntity.class)
                .eq(DictRuleConditionEntity::getType, DictBasicTypeEnum.COMPARE.getType()));
        return list.stream().collect(Collectors.toMap(DictRuleConditionEntity::getKey, DictRuleConditionEntity::getValue, (o1, o2) -> o1));
    }
    @Override
    public List<CfgConditionDTO.TreeDTO> tree(String type) {
        List<CfgConditionEntity> conditionEntityList = this.list(Wrappers.<CfgConditionEntity>lambdaQuery()
                .eq(CfgConditionEntity::getRuleType, type));
        List<CfgConditionDTO.TreeDTO> resultList = new ArrayList<>(conditionEntityList.size());
        Map<String, String> map = getDictRuleMap();
        for (CfgConditionEntity item : conditionEntityList) {
            String conditionField = item.getConditionField();
            CfgConditionDTO.TreeDTO tree = new CfgConditionDTO.TreeDTO();
            tree.setConditionField(conditionField);
            String logicStr = item.getLogic();
            List<String> logicList = Arrays.asList(logicStr.split(","));
            List<CfgConditionDTO.TreeDTO> childrenList = new ArrayList<>(logicList.size());
            for (String logic : logicList) {
                CfgConditionDTO.TreeDTO children = new CfgConditionDTO.TreeDTO();
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
    public List<CfgConditionEntity> listByFields(List<String> fieldList) {
        if (CollectionUtils.isEmpty(fieldList)){
            return Collections.emptyList();
        }
        return list(Wrappers.<CfgConditionEntity>lambdaQuery().in(CfgConditionEntity::getConditionField, fieldList));
    }
}
