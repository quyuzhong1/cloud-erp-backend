package com.erp.server.dmp.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.utils.ApplicationContextUtils;
import com.common.core.enums.RuleCompareEnum;
import com.erp.model.dmp.entity.RuleConditionEntity;
import com.erp.model.dmp.entity.DictRuleConditionEntity;
import com.erp.model.dmp.entity.CfgConditionEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.dmp.mapper.RuleConditionMapper;
import com.erp.server.dmp.service.CfgConditionService;
import com.erp.server.dmp.service.DictRuleConditionService;
import com.erp.server.dmp.service.RuleConditionService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.RuleConditionDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 规则条件表 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2025-01-20
 */
@Slf4j
@Service
public class RuleConditionServiceImpl extends SuperServiceImpl<RuleConditionMapper, RuleConditionEntity> implements RuleConditionService {
    @Autowired
    private OperateLogService operateLogService;

    @Resource
    private DictRuleConditionService dictRuleConditionService;

    @Resource
    private CfgConditionService cfgConditionService;
    
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(RuleConditionDTO.AddDTO addDTO) {
        RuleConditionEntity ruleConditionEntity = new RuleConditionEntity();
        BeanMapperUtils.copy(addDTO, ruleConditionEntity);

        // 数据处理
        handleData(ruleConditionEntity);

        log.info("开始新增规则条件单");
        boolean save = super.save(ruleConditionEntity);
        if(!save) {
            throw new ServiceException("规则条件单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "规则条件单" , ruleConditionEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, ruleConditionEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(ruleConditionEntity.getId(), ruleConditionEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(RuleConditionDTO.UpdateDTO addOrUpdateDTO) {
        RuleConditionEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "规则条件单"));
        RuleConditionEntity ruleConditionEntity =  BeanMapperUtils.map(RuleConditionEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(ruleConditionEntity);
        log.info("编辑 开始修改规则条件单数据，id：【{}】", old.getId());
        boolean save = super.updateById(ruleConditionEntity);
        if(!save) {
            throw new ServiceException("规则条件单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录规则条件单日志数据，id：【{}】", ruleConditionEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), ruleConditionEntity.getId(), "规则条件单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, ruleConditionEntity, null, ruleConditionEntity.getId(), msg);
        return Boolean.TRUE;
    }
    private List<RuleConditionEntity> listDbByRuleId(String ruleId) {
        return this.lambdaQuery().eq(RuleConditionEntity::getRuleId, ruleId).
                orderByAsc(RuleConditionEntity::getIndex).list();
    }

    @Override
    public List<RuleConditionDTO.ViewDTO> listByRuleId(String ruleId, String type) {
        List<RuleConditionEntity> ruleConditionList = this.listDbByRuleId(ruleId);
        List<RuleConditionDTO.ViewDTO> viewList = BeanMapper.copyList(ruleConditionList, RuleConditionDTO.ViewDTO.class);
        String logicType = DictBasicTypeEnum.COMPARE.getType();
        List<DictRuleConditionEntity> dictRuleConditionList = dictRuleConditionService.listDbByTypes(Arrays.asList(type, logicType));

        List<BaseDropDownDTO.CommonDTO> fieldList = dictRuleConditionService.listRuleField();
        for (RuleConditionDTO.ViewDTO item : viewList) {
            String field = item.getField();
            String fieldName = fieldList.stream().filter(d -> d.getCode().equals(field)).findFirst().
                    map(BaseDropDownDTO.CommonDTO::getValue).orElse("");
            item.setFieldName(fieldName);
            String compare = item.getCompare();
            String compareName = dictRuleConditionList.stream().filter(d -> d.getKey().equals(compare)).findFirst().
                    map(DictRuleConditionEntity::getValue).orElse("");
            item.setCompareName(compareName);
            String logic = item.getLogic();
            String logicName = "";
            if (StringUtils.isNotBlank(logic)) {
                logicName = DictBasicTypeEnum.getName(logic);
            }
            item.setLogicName(logicName);

        }
        return viewList;
    }
    /**
     * 新增修改处理数据
     */
    private void handleDataList(List<RuleConditionEntity> ruleConditionList) {
        for (RuleConditionEntity item : ruleConditionList) {
            String compare = item.getCompare();
            if (RuleCompareEnum.IS_NULL.getCode().equals(compare)) {
                item.setValue("");
            }
        }
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRuleCondition(String ruleId, List<RuleConditionDTO.AddDTO> conditionList) {
        if (CollectionUtils.isEmpty(conditionList)) {
            return;
        }
        int i = 1;
        for (RuleConditionDTO.AddDTO item : conditionList) {
            item.setIndex(i);
            item.setName(CharSequenceUtil.isBlank(item.getName()) ? item.getValue() : item.getName());
            i++;
        }
        List<RuleConditionEntity> ruleConditionList = BeanMapper.copyList(conditionList, RuleConditionEntity.class);
        ruleConditionList.forEach(r -> r.setRuleId(ruleId));
        handleDataList(ruleConditionList);
        RuleConditionServiceImpl bean = ApplicationContextUtils.getBean(RuleConditionServiceImpl.class);
        bean.saveBatch(ruleConditionList);
    }

    @Override
    public void updateRuleCondition(String ruleId, List<RuleConditionDTO.UpdateDTO> conditionList) {
        List<CfgConditionEntity> cfgConditionList = cfgConditionService.list();
        List<RuleConditionDTO.UpdateDTO> updateList = conditionList.stream().filter(c -> StringUtils.isNotBlank(c.getId())).collect(Collectors.toList());
        int i = 1;
        for (RuleConditionDTO.UpdateDTO item : conditionList) {
            item.setIndex(i);
            item.setName(CharSequenceUtil.isBlank(item.getName()) ? item.getValue() : item.getName());
            i++;
        }
        String moduleType = ModuleTypeEnum.RULE_ORDER_APPROVAL.getCode();
        List<RuleConditionEntity> saveOrUpdateList = BeanMapper.copyList(conditionList, RuleConditionEntity.class);
        for (RuleConditionEntity obj : saveOrUpdateList) {
            obj.setRuleId(ruleId);
            String field = obj.getField();
            String fieldName = cfgConditionList.stream().filter(c -> c.getConditionField().equals(field)).findFirst().
                    map(CfgConditionEntity::getConditionFieldName).orElse(field);
            obj.setFieldName(fieldName);
        }

        List<RuleConditionEntity> dbList = this.listDbByRuleId(ruleId);
        for (RuleConditionEntity item : dbList) {
            String field = item.getField();
            String fieldName = cfgConditionList.stream().filter(c -> c.getConditionField().equals(field)).findFirst().
                    map(CfgConditionEntity::getConditionFieldName).orElse(field);
            item.setFieldName(fieldName);
        }
        List<Pair<String, String>> pairList = updateList.stream().map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        //获取到删除的ids
        List<String> deleteIdList = getDeleteIds(pairList, dbList);
        List<RuleConditionEntity> removeList = dbList.stream().filter(r -> deleteIdList.contains(r.getId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        List<Pair<String, String>> removePairList = removeList.stream().map(obj -> new Pair<>(ruleId, obj.getFieldName())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("删除了一个条件字段【%s】", moduleType, removePairList, "编辑操作");
        //添加的条件
        List<RuleConditionEntity> addList = saveOrUpdateList.stream().filter(r -> StringUtils.isBlank(r.getId())).collect(Collectors.toList());
        List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(ruleId, obj.getFieldName())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("添加一个条件字段【%s】", moduleType, addPairList, "添加操作");
        //修改的
        List<RuleConditionEntity> updateRuleConditionList = saveOrUpdateList.stream().filter(r -> StringUtils.isNotBlank(r.getId())).collect(Collectors.toList());
        for (RuleConditionEntity updateItem : updateRuleConditionList) {
            RuleConditionEntity old = dbList.stream().filter(r -> r.getId().equals(updateItem.getId())).findFirst().orElse(null);
            if (Objects.nonNull(old)) {
                operateLogService.addModuleOperateLogByObj(old, updateItem, moduleType, ruleId,  CharSequenceUtil.format("修改了第【{}】条订单规则",updateItem.getIndex()));
            }
        }
        handleDataList(saveOrUpdateList);
        RuleConditionServiceImpl bean = ApplicationContextUtils.getBean(RuleConditionServiceImpl.class);
        bean.saveOrUpdateBatch(saveOrUpdateList);
    }

    /**
     * 获取到删除的ids
     *
     * @param pairList
     * @param dbList
     * @return
     */
    private List<String> getDeleteIds(List<Pair<String, String>> pairList, List<RuleConditionEntity> dbList) {
        List<String> ids = pairList.stream().filter(g -> StringUtils.isNotBlank(g.getKey())).
                map(Pair::getKey).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(RuleConditionEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(RuleConditionEntity ruleConditionEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
