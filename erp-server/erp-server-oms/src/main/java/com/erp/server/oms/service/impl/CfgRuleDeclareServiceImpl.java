package com.erp.server.oms.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.dto.SpElExpressionDTO;
import com.common.core.entity.ConditionElement;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.server.rule.SpElServer;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.CfgRuleDeclareDTO;
import com.erp.model.oms.dto.RuleConditionDTO;
import com.erp.model.oms.entity.CfgRuleDeclareEntity;
import com.erp.model.oms.entity.RuleConditionEntity;
import com.erp.model.oms.entity.SoB2cDeclareProductEntity;
import com.erp.model.oms.enums.DeclareLabelTypeEnum;
import com.erp.model.oms.enums.DeclareTypeEnum;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.convert.B2cOrderConverter;
import com.erp.server.oms.mapper.CfgRuleDeclareMapper;
import com.erp.server.oms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 申报规则表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-05-08
 */
@Slf4j
@Service
public class CfgRuleDeclareServiceImpl extends SuperServiceImpl<CfgRuleDeclareMapper, CfgRuleDeclareEntity> implements CfgRuleDeclareService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private SpElServer spElServer;
    @Autowired
    private RuleConditionService ruleConditionService;
    @Resource
    private SoB2cDeclareProductService soB2cDeclareProductService;
    @Autowired
    private SoB2cLogisticsService soB2cLogisticsService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(CfgRuleDeclareDTO.AddDTO addDTO) {
        List<RuleConditionDTO.AddDTO> conditionList = addDTO.getConditionList();
        List<ConditionElement> conditionElementList = conditionList.stream().
                map(c -> new ConditionElement(c.getLeftBracket(), c.getField(),
                        c.getCompare(), c.getValue(),
                        c.getRightBracket(), c.getLogic(), "")).collect(Collectors.toList());
        SpElExpressionDTO sqElDTO = spElServer.getConditionExpression(conditionElementList, Map.class);
        String expression = sqElDTO.getExpression();
        Boolean checkResult = spElServer.checkExpressionIsEnabled(expression);
        if (!checkResult) {
            throw new ServiceException(ApiError.ERROR_RULE_EXPRESSION_ERROR);
        }
        CfgRuleDeclareEntity entity = new CfgRuleDeclareEntity();
        BeanMapperUtils.copy(addDTO, entity);
        handleData(entity);
        boolean save = super.save(entity);
        if (!save) {
            throw new ServiceException("申报规则单保存失败");
        }

        String id = entity.getId();
        //保存规则条件
        ruleConditionService.saveRuleCondition(id, conditionList);
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据名称为【{}】", UserContext.getDefaultLoginUser().getUserName(), "申报规则单", entity.getName());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.RULE_DECLARE.getCode(), id, "新增操作");
        return entity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgRuleDeclareDTO.UpdateDTO updateDTO) {
        String id = updateDTO.getId();
        CfgRuleDeclareEntity old = super.getById(id);
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "申报规则单"));
        List<RuleConditionDTO.UpdateDTO> conditionList = updateDTO.getConditionList();
        List<ConditionElement> conditionElementList = conditionList.stream().
                map(c -> new ConditionElement(c.getLeftBracket(), c.getField(),
                        c.getCompare(), c.getValue(),
                        c.getRightBracket(), c.getLogic(), "")).collect(Collectors.toList());
        SpElExpressionDTO sqElDTO = spElServer.getConditionExpression(conditionElementList, Map.class);
        String expression = sqElDTO.getExpression();
        Boolean checkResult = spElServer.checkExpressionIsEnabled(expression);
        if (!checkResult) {
            throw new ServiceException(ApiError.ERROR_RULE_EXPRESSION_ERROR);
        }
        CfgRuleDeclareEntity entity = BeanMapperUtils.map(CfgRuleDeclareEntity.class, updateDTO);
        // 数据处理
        handleData(entity);
//        boolean save = super.updateById(entity);
        boolean save = lambdaUpdate().eq(CfgRuleDeclareEntity::getId,entity.getId())
                .set(CfgRuleDeclareEntity::getName,entity.getName())
                .set(CfgRuleDeclareEntity::getPriority,entity.getPriority())
                .set(CfgRuleDeclareEntity::getDisabled,entity.getDisabled())
                .set(CfgRuleDeclareEntity::getRemark,entity.getRemark())
                .set(CfgRuleDeclareEntity::getDeclareCn,entity.getDeclareCn())
                .set(CfgRuleDeclareEntity::getDeclareEn,entity.getDeclareEn())
                .set(CfgRuleDeclareEntity::getToCustomsCode,entity.getToCustomsCode())
                .set(CfgRuleDeclareEntity::getToDeclarePrice,entity.getToDeclarePrice())
                .set(CfgRuleDeclareEntity::getToCurrency,entity.getToCurrency())
                .set(CfgRuleDeclareEntity::getToCurrencySymbol,entity.getToCurrencySymbol())
                .set(CfgRuleDeclareEntity::getRate,entity.getRate())
                .set(CfgRuleDeclareEntity::getToDeclarePriceType,entity.getToDeclarePriceType())
                .set(CfgRuleDeclareEntity::getMinDeclarePrice,entity.getMinDeclarePrice())
                .set(CfgRuleDeclareEntity::getMaxDeclarePrice,entity.getMaxDeclarePrice())
                .update(new CfgRuleDeclareEntity());
        if (!save) {
            throw new ServiceException("申报规则单保存失败");
        }
        ruleConditionService.updateRuleCondition(id, conditionList);
        // 记录主单操作日志
        String msg = StrUtil.format("用户【{}】编辑名称为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), entity.getName(), "申报规则单");
        operateLogService.addModuleOperateLogByObj(old, entity, ModuleTypeEnum.RULE_DECLARE.getCode(), entity.getId(), msg);
        return Boolean.TRUE;
    }

    /**
     * 申报规则分页查询
     * @param dto
     * @return
     */
    @Override
    public PagingVO<CfgRuleDeclareDTO.PagingViewDTO> paging(PagingDTO<CfgRuleDeclareDTO.PagingParamDTO> dto) {
        CfgRuleDeclareDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        return new PagingVO<>(pageData);

    }

    @Override
    public CfgRuleDeclareDTO.ViewDTO view(String id) {
        CfgRuleDeclareEntity entity = this.getById(id);
        Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "申报规则单"));
        CfgRuleDeclareDTO.ViewDTO view = new CfgRuleDeclareDTO.ViewDTO();
        BeanMapper.copy(entity, view);
        String type = DictBasicTypeEnum.FIELD.getType();
        List<RuleConditionDTO.ViewDTO> conditionList = ruleConditionService.listByRuleId(id, type);
        view.setConditionList(conditionList);
        return view;
    }

    @Override
    public Boolean updateStatus(UpdateStateDTO dto) {
        CfgRuleDeclareEntity entity = this.getById(dto.getId());
        Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "申报规则单"));
        Boolean disabled = entity.getDisabled();
        if (disabled.equals(dto.getState())) {
            throw new ServiceException(ApiError.ERROR_98027);
        }
        String content = String.format("启用状态[%s]变更为[%s]", disabled ? "停用" : "启用", disabled ? "启用" : "停用");
        entity.setDisabled(dto.getState());
        operateLogService.addModuleOperateLog(content, ModuleTypeEnum.RULE_DECLARE.getCode(), dto.getId(), "状态变更");
        return this.updateById(entity);

    }

    /**
     * 获取申报规则匹配结果
     *
     * @param map
     * @param maxCustomsAmount
     * @param minCustomsAmount
     * @param isUpdate
     * @param declareProductList
     * @return
     */
    @Override
    public void getRuleDeclareMatchResult(HashMap<String, Object> map, BigDecimal maxCustomsAmount, BigDecimal minCustomsAmount, Boolean isUpdate, List<SoB2cDeclareProductEntity> declareProductList) {
        if (Objects.isNull(map)) {
            return;
        }
        List<Map<String, Object>> mapList = (List<Map<String, Object>>) map.get("detailList");
        //要匹配渠道id 是空的 如果有就 不用匹配了返回成功
        List<CfgRuleDeclareEntity> cfgRuleDeclareList = this.listOrderByPriority();
        List<String> ruleIdList = cfgRuleDeclareList.stream().map(CfgRuleDeclareEntity::getId).collect(Collectors.toList());
        //规则条件
        List<RuleConditionEntity> allRuleConditionList = ruleConditionService.listDbRuleIds(ruleIdList);
        List<SoB2cDeclareProductEntity> addList = new ArrayList<>(mapList.size());
        //根据明细进行遍历规则
        for (Map<String, Object> detailMap : mapList){
            SoB2cDeclareProductEntity entity = compareRules(detailMap, cfgRuleDeclareList,allRuleConditionList);
            if (Objects.isNull(entity)){
                entity = B2cOrderConverter.INSTANCE.convertDeclareProductByMap(detailMap);
            }
            //根据渠道进行重置目的国申报单价 上下限
            resetToDeclarePrice(entity,detailMap,maxCustomsAmount, minCustomsAmount);
            addList.add(entity);
        }
        if (Objects.nonNull(isUpdate) && isUpdate){
            //删除已存在申报信息
            soB2cDeclareProductService.removeBySoId(String.valueOf(map.get("id")));
        }
        //判断是否更新规则 isUpdate
        if (CollectionUtil.isNotEmpty(addList)){
            soB2cDeclareProductService.saveBatch(addList);
            if (Objects.nonNull(isUpdate) && isUpdate){
                //批量添加操作日志
                batchAddDeclareOperateLog(declareProductList, addList, "批量更新报关");
            }else {
                String msg = StrUtil.format("自动生成报关信息");
                operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), String.valueOf(map.get("id")), "报关信息生成");
            }
        }
    }

    private void batchAddDeclareOperateLog(List<SoB2cDeclareProductEntity> declareProductList, List<SoB2cDeclareProductEntity> addList, String operation) {
        List<Pair<String, String>> updateLogPairList = new ArrayList<>();
        for (SoB2cDeclareProductEntity newDeclareProduct : addList) {
            SoB2cDeclareProductEntity old = declareProductList.stream().filter(e -> Objects.nonNull(e) && e.getSoDetailId().equals(newDeclareProduct.getSoDetailId())
                    && e.getSkuId().equals(newDeclareProduct.getSkuId())).findFirst().orElse(new SoB2cDeclareProductEntity());
            List<String> contentList = operateLogService.getContentByObj(old, newDeclareProduct, "");
            contentList.forEach(content ->{
                Pair<String, String> pair = new Pair<>(newDeclareProduct.getSoId(), content);
                updateLogPairList.add(pair);
            });
        }
        if (CollectionUtils.isNotEmpty(updateLogPairList)) {
            operateLogService.batchAddModuleOperateLog(StrUtil.format("用户【{}】编辑销售订单申报信息",UserContext.getDefaultLoginUser().getUserName())+"，【%s】", ModuleTypeEnum.SO_B2C_DECLARE.getCode(), updateLogPairList,"编辑操作");
        }

    }


    /**
     * 根据渠道进行 目的国申报价上下限设置
     *
     * @param entity
     * @param detailMap
     * @param maxCustomsAmount
     * @param minCustomsAmount
     */
    private void resetToDeclarePrice(SoB2cDeclareProductEntity entity, Map<String, Object> detailMap, BigDecimal maxCustomsAmount, BigDecimal minCustomsAmount) {
        //表示最大的报关价还小于 目的过申报价
        if (Objects.nonNull(entity.getToDeclarePrice()) && Objects.nonNull(maxCustomsAmount)
                && maxCustomsAmount.compareTo(BigDecimal.ZERO) != 0 && maxCustomsAmount.compareTo(entity.getToDeclarePrice()) < 0) {
            entity.setToDeclarePrice(maxCustomsAmount);
        }
        //表示最小的报关价还小于 目的过申报价
        if (Objects.nonNull(entity.getToDeclarePrice()) && Objects.nonNull(minCustomsAmount)
                && minCustomsAmount.compareTo(BigDecimal.ZERO) != 0 && entity.getToDeclarePrice().compareTo(minCustomsAmount) < 0) {
            entity.setToDeclarePrice(minCustomsAmount);
        }

        //申报标签
        Object toDeclarePriceObj = detailMap.get("toDeclarePrice");
        int compare = MathUtil.compareTo(entity.getToDeclarePrice(), toDeclarePriceObj );
        if (compare > 0){
            //高申报
            entity.setDeclareLabel(DeclareLabelTypeEnum.HIGH.getCode());
        }else if (compare < 0){
            //低申报
            entity.setDeclareLabel(DeclareLabelTypeEnum.LOW.getCode());
        }else {
            //正常申报
            entity.setDeclareLabel(DeclareLabelTypeEnum.NORMAL.getCode());
        }
    }

    private SoB2cDeclareProductEntity compareRules(Map<String, Object> detailMap, List<CfgRuleDeclareEntity> cfgRuleDeclareList,List<RuleConditionEntity> allRuleConditionList) {
        for (CfgRuleDeclareEntity item : cfgRuleDeclareList) {
            String ruleId = item.getId();
            List<RuleConditionEntity> ruleConditionList = allRuleConditionList.stream().
                    filter(r -> r.getRuleId().equals(ruleId)).
                    sorted(Comparator.comparing(RuleConditionEntity::getIndex)).collect(Collectors.toList());

            List<ConditionElement> conditionElementList = BeanMapper.copyList(ruleConditionList, ConditionElement.class);
            //获取到表达式
            Boolean matchResult = spElServer.matchDetailExpressionByConditionList(conditionElementList, detailMap);
            if (matchResult) {
                //返回申报明细
                SoB2cDeclareProductEntity entity = B2cOrderConverter.INSTANCE.convertDeclareProductByMap(detailMap);
                //重置固定值
                if (StringUtils.isNotEmpty(item.getDeclareCn())){
                    entity.setDeclareCn(item.getDeclareCn());
                }
                if (StringUtils.isNotEmpty(item.getDeclareEn())){
                    entity.setDeclareEn(item.getDeclareEn());
                }
                if (StringUtils.isNotEmpty(item.getToCustomsCode())){
                    entity.setToCustomsCode(item.getToCustomsCode());
                }
                if (StringUtils.isNotEmpty(item.getToDeclarePriceType())){
                    //固定申报
                    if (DeclareTypeEnum.FIXED_PRICE.getCode().equals(item.getToDeclarePriceType())){
                        entity.setToDeclarePrice(item.getToDeclarePrice());
                        entity.setToCurrency(item.getToCurrency());
                        entity.setToCurrencySymbol(item.getToCurrencySymbol());
                    }else if (DeclareTypeEnum.PRICE_PERCENTAGE.getCode().equals(item.getToDeclarePriceType())){
                        BigDecimal toDeclarePrice = entity.getToDeclarePrice();
                        BigDecimal rate = item.getRate();
                        BigDecimal ratePercent = MathUtil.divide(rate, MathUtil.BigDecimal_100);
                        BigDecimal toDeclarePrice1 = MathUtil.multiply(toDeclarePrice, ratePercent);
                        //重置目的国申报价
                        if (Objects.nonNull(item.getMaxDeclarePrice()) && toDeclarePrice1.compareTo(item.getMaxDeclarePrice()) > 0){
                            toDeclarePrice1 = item.getMaxDeclarePrice();
                        }else if (Objects.nonNull(item.getMinDeclarePrice()) && item.getMinDeclarePrice().compareTo(toDeclarePrice1) > 0){
                            toDeclarePrice1 = item.getMinDeclarePrice();
                        }
                        entity.setToDeclarePrice(toDeclarePrice1);
                        entity.setToCurrency(item.getToCurrency());
                        entity.setToCurrencySymbol(item.getToCurrencySymbol());
                    }
                }
                return entity;
            }
        }
        return null;
    }

    private List<CfgRuleDeclareEntity> listOrderByPriority() {
        return this.lambdaQuery().eq(CfgRuleDeclareEntity::getDisabled,Boolean.FALSE).orderByAsc(CfgRuleDeclareEntity::getPriority).orderByDesc(CfgRuleDeclareEntity::getUpdateTime).list();
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgRuleDeclareEntity entity) {
        if (StringUtils.isEmpty(entity.getToCurrency())){
            entity.setToCurrency(CurrencyEnum.USD.getCurrencyCode());
        }
        if (StringUtils.isEmpty(entity.getToCurrencySymbol())){
            entity.setToCurrencySymbol(CurrencyEnum.USD.getCurrencySymbol());
        }
    }
}
