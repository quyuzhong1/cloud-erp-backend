package com.erp.server.scm.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.entity.DictRuleConditionEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.scm.entity.CfgConditionEntity;
import com.erp.server.scm.mapper.CfgConditionMapper;
import com.erp.server.scm.service.CfgConditionService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.scm.dto.CfgConditionDTO;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-06-16
 */
@Slf4j
@Service
public class CfgConditionServiceImpl extends SuperServiceImpl<CfgConditionMapper, CfgConditionEntity> implements CfgConditionService {

    @Override
    public List<CfgConditionDTO.CommonDTO> listByType(String type) {
        List<CfgConditionEntity> list = lambdaQuery()
                .eq(CfgConditionEntity::getRuleType, type)
                .list();
        return BeanMapper.copyList(list,CfgConditionDTO.CommonDTO.class);
    }

    @Override
    public List<CfgConditionDTO.TreeDTO> tree(String type) {

        List<CfgConditionEntity> conditionEntityList = lambdaQuery().eq(CfgConditionEntity::getRuleType, type).list();

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

    private Map<String, String> getDictRuleMap(){
        List<DictRuleConditionEntity> list = FeignQuery.list(FeignQuery.create(DictRuleConditionEntity.class)
                .eq(DictRuleConditionEntity::getType, DictBasicTypeEnum.COMPARE.getType()));
        return list.stream().collect(Collectors.toMap(DictRuleConditionEntity::getKey, DictRuleConditionEntity::getValue, (o1, o2) -> o1));
    }
}
