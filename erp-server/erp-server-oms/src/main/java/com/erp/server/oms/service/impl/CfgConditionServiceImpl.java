package com.erp.server.oms.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.enums.RuleCompareEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.CfgConditionDTO;
import com.erp.model.oms.entity.CfgConditionEntity;
import com.erp.model.oms.enums.ConditionSourceTypeEnum;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.server.oms.mapper.CfgConditionMapper;
import com.erp.server.oms.service.CfgConditionService;
import com.erp.server.oms.service.DictRuleConditionService;
import com.erp.server.oms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * <p>
 * 条件配置表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-30
 */
@Slf4j
@Service
public class CfgConditionServiceImpl extends SuperServiceImpl<CfgConditionMapper, CfgConditionEntity> implements CfgConditionService {

    @Resource
    private DictRuleConditionService dictRuleConditionService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(CfgConditionDTO.AddDTO addDTO) {
        CfgConditionEntity cfConditionEntity = new CfgConditionEntity();
        BeanMapperUtils.copy(addDTO, cfConditionEntity);

        log.info("开始新增条件配置单");
        boolean save = super.save(cfConditionEntity);
        if (!save) {
            throw new ServiceException("条件配置单保存失败");
        }
        return cfConditionEntity.getId();
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgConditionDTO.UpdateDTO updateDTO) {
        CfgConditionEntity old = super.getById(updateDTO.getId());
        if(null == old){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "条件配置单");
        }
        CfgConditionEntity cfConditionEntity = BeanMapperUtils.map(CfgConditionEntity.class, updateDTO);

        // 数据处理
        log.info("编辑 开始修改条件配置单数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfConditionEntity);
        if (!save) {
            throw new ServiceException("条件配置单保存失败");
        }
        return Boolean.TRUE;
    }

    /**
     * 根据添加code 获取到逻辑关系
     *
     * @param conditionCode
     * @return
     */
    @Override
    public List<CfgConditionDTO.CommonDTO> listByConditionCode(String conditionCode) {
        List<CfgConditionDTO.CommonDTO> list = baseMapper.listByConditionCode(conditionCode);
        String type = DictBasicTypeEnum.COMPARE.getType();
        List<BaseDropDownDTO.CommonDTO> dictRuleConditionList = dictRuleConditionService.listByType(type);
        for (CfgConditionDTO.CommonDTO item : list) {
            String logic = item.getLogic();
            String logicName = dictRuleConditionList.stream().filter(d -> d.getCode().equals(logic)).
                    findFirst().map(BaseDropDownDTO.CommonDTO::getValue).orElse("");
            item.setLogicName(logicName);
        }
        return list;
    }

    /**
     * 获取到所有的条件值
     *
     * @param
     * @return java.util.List<com.erp.model.oms.dto.CfConditionDTO.ListDTO>
     * @author yl
     * @date 2023-10-08 14:45
     */
    @Override
    public List<CfgConditionDTO.ListDTO> listAllCondition() {
        return baseMapper.listConditionByType(null, ConditionSourceTypeEnum.ORDER.getCode());
    }

    @Override
    public List<CfgConditionDTO.ListDTO> listDeclareCondition() {
        List<String> typeList = Arrays.asList("dictPlatform", "shop", "destCountry", "skuNo", "logisticsChannelId");
        return baseMapper.listConditionByType(typeList, ConditionSourceTypeEnum.ORDER.getCode());
    }

    @Override
    public List<CfgConditionDTO.ListDTO> listOrderHandleCondition() {
        List<String> typeList = Arrays.asList("dictPlatform", "shop", "destCountry", "logisticsChannelId");
        return baseMapper.listConditionByType(typeList, ConditionSourceTypeEnum.ORDER.getCode());
    }

    @Override
    public List<CfgConditionDTO.ListDTO> listInvoiceHandleCondition() {
        List<String> typeList = Arrays.asList("dictPlatform", "shop", "destCountry", "nfeInvoiceStatus");
        return baseMapper.listConditionByType(typeList, null);
    }

    @Override
    public List<CfgConditionDTO.ListDTO> listHandleConditionByType(List<String> typeList) {
        if (CollUtil.isEmpty(typeList)){
            return Collections.emptyList();
        }
        return baseMapper.listConditionByType(typeList, null);
    }


    /**
     * 条件树结构
     *
     * @param
     * @return java.util.List<com.erp.model.oms.dto.CfConditionDTO.TreeDTO>
     * @author yl
     * @date 2023-10-08 15:09
     */
    @Override
    public List<CfgConditionDTO.TreeDTO> tree() {
        List<CfgConditionEntity> conditionEntityList = this.list();
        List<CfgConditionDTO.TreeDTO> resultList = new ArrayList<>(conditionEntityList.size());
        Map<String, String> map = new HashMap<>();
        for (RuleCompareEnum item : RuleCompareEnum.values()) {
            map.put(item.getCode(), item.getName());
        }

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
        if(CollectionUtils.isEmpty(fieldList)){
           return Collections.emptyList();
        }
        return this.lambdaQuery().in(CfgConditionEntity::getConditionField,fieldList).list();
    }
}
