package com.erp.server.mrp.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.mrp.dto.CfgRuleSalesFormulaDTO;
import com.erp.model.mrp.dto.CfgRuleSalesQtyDTO;
import com.erp.model.mrp.entity.CfgRuleSalesFormulaEntity;
import com.erp.model.mrp.entity.CfgRuleSalesQtyEntity;
import com.erp.model.mrp.enums.CfgRulePercentEnum;
import com.erp.model.mrp.enums.CfgRuleSalesFormulaDefaultTypeEnum;
import com.erp.model.mrp.enums.CfgRuleSalesFormulaTypeEnum;
import com.erp.model.mrp.enums.CfgRuleStockingRatioTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.mrp.mapper.CfgRuleSalesFormulaMapper;
import com.erp.server.mrp.service.CfgRuleSalesFormulaService;
import com.erp.server.mrp.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 销量公式（规则设置） 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
@Slf4j
@Service
public class CfgRuleSalesFormulaServiceImpl extends SuperServiceImpl<CfgRuleSalesFormulaMapper, CfgRuleSalesFormulaEntity> implements CfgRuleSalesFormulaService {
    @Resource
    private OperateLogService operateLogService;

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<CfgRuleSalesFormulaDTO.UpdateDTO> salesFormulaList, CfgRuleSalesQtyEntity cfgRuleSalesQtyEntity, String skuType, Boolean isCustom) {
        if (CollectionUtils.isEmpty(salesFormulaList)) {
            salesFormulaList = Collections.emptyList();
        }
        List<CfgRuleSalesFormulaEntity> list = BeanMapperUtils.copyList(CfgRuleSalesFormulaEntity.class, salesFormulaList);

        //原信息
        List<CfgRuleSalesFormulaEntity> oldList = listBySalesQtyIdList(Collections.singletonList(cfgRuleSalesQtyEntity.getId()), skuType);
        //自定义更新无需删除
        if (Boolean.FALSE.equals(isCustom)) {
            //删除明细
            List<String> deleteIds = getDeleteIds(list, oldList);
            if (CollectionUtils.isNotEmpty(deleteIds)) {
                this.removeByIds(deleteIds);

                // 数据处理
                oldList = oldList.stream().filter(obj -> !deleteIds.contains(obj.getId())).collect(Collectors.toList());
            }
        }
        if (CollectionUtils.isEmpty(list)) {
            return  Boolean.TRUE;
        }
        // 数据处理
        handleData(list,oldList,cfgRuleSalesQtyEntity.getId(),isCustom, skuType);
        log.info("编辑 开始修改销量公式（规则设置）数据，id：【{}】", cfgRuleSalesQtyEntity.getId());
        boolean save = super.saveOrUpdateBatch(list);
        if(!save) {
            throw new ServiceException("销量公式（规则设置）保存失败");
        }
        addOperateLog(list,cfgRuleSalesQtyEntity, skuType);
        return Boolean.TRUE;
    }
    /**
     * 添加日志
     *
     * @param list
     * @param salesQtyEntity
     * @param skuType
     * @author will
     * @date 2024/9/12 10:26
     */
    private void addOperateLog (List<CfgRuleSalesFormulaEntity> list, CfgRuleSalesQtyEntity salesQtyEntity, String skuType) {
        Map<String, List<CfgRuleSalesFormulaEntity>> map = list.stream().collect(Collectors.groupingBy(obj -> obj.getType().concat(CharSequenceUtil.blankToDefault(obj.getDefaultType(),""))));
        //日志
        for (Map.Entry<String, List<CfgRuleSalesFormulaEntity>> entry : map.entrySet()) {
            List<CfgRuleSalesFormulaEntity> value = entry.getValue();
            StringBuilder msg = new StringBuilder();
            msg.append(CharSequenceUtil.format("{}_{}日销量：<br>",CfgRuleStockingRatioTypeEnum.getName(skuType),CfgRuleSalesFormulaTypeEnum.getName(value.get(0).getType())));
            for (CfgRuleSalesFormulaEntity formulaEntity : value) {
                if (!CharSequenceUtil.equals(formulaEntity.getType(),CfgRuleSalesFormulaTypeEnum.DEFAULT.getCode()) ) {
                    msg.append(CharSequenceUtil.format("•序号【{}】、名称【{}】、时间段【{}】<br>" ,formulaEntity.getIndex(),formulaEntity.getName(),CharSequenceUtil.format("{}~{}",formulaEntity.getStartDate(),formulaEntity.getEndDate())));
                }
                if (ObjectUtil.isNotEmpty(formulaEntity.getFixedValue()) && (CharSequenceUtil.equals(formulaEntity.getType(),CfgRuleSalesFormulaTypeEnum.FIXED.getCode()) || CharSequenceUtil.equals(formulaEntity.getDefaultType(),CfgRuleSalesFormulaDefaultTypeEnum.FIXED.getCode())) ) {
                    msg.append(CharSequenceUtil.format("•固定值：{}<br>", formulaEntity.getFixedValue()));
                } else {
                    JSONObject percentJson = formulaEntity.getPercentJson();
                    percentJson.forEach((key, value1) -> msg.append(CharSequenceUtil.format("•{}：{}<br>", CfgRulePercentEnum.getName(key), value1)));
                }
            }
            operateLogService.addModuleOperateLog(msg.toString(), ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), CharSequenceUtil.blankToDefault(salesQtyEntity.getRefId(),salesQtyEntity.getId()), "销量");
        }
    }

    @Override
    public List<CfgRuleSalesFormulaEntity> listBySalesQtyIdList(List<String> salesQtyIdList) {
        return listBySalesQtyIdList(salesQtyIdList, null);
    }

    private List<CfgRuleSalesFormulaEntity> listBySalesQtyIdList(List<String> salesQtyIdList, String skuType) {
        if (CollectionUtils.isEmpty(salesQtyIdList)) {
            return  Collections.emptyList();
        }
        List<CfgRuleSalesFormulaEntity> list = lambdaQuery().in(CfgRuleSalesFormulaEntity::getSalesQtyId, salesQtyIdList)
                .eq(StringUtils.isNotBlank(skuType), CfgRuleSalesFormulaEntity::getSkuType, skuType).list();
        if (CollectionUtils.isEmpty(list)) {
            return  Collections.emptyList();
        }
        for (CfgRuleSalesFormulaEntity formulaEntity : list) {
            //百分比json
            CfgRuleSalesFormulaDTO.PercentJsonDTO percentJsonDTO = JSONUtil.toBean(formulaEntity.getPercentJson(), CfgRuleSalesFormulaDTO.PercentJsonDTO.class);
            formulaEntity.setPercentJsonDTO(percentJsonDTO);
            //时间
            formulaEntity.setDateList(Arrays.asList(formulaEntity.getStartDate(),formulaEntity.getEndDate()));
        }
        return list;
    }

    @Override
    public void deleteBySalesQtyId(String salesQtyId) {
        lambdaUpdate().eq(CfgRuleSalesFormulaEntity::getSalesQtyId,salesQtyId).remove();
    }

    @Override
    public List<CfgRuleSalesQtyDTO.StrategyFormulaResultDTO> listFormulaBySalesId(String id) {
        List<CfgRuleSalesFormulaEntity> list = list(Wrappers.<CfgRuleSalesFormulaEntity>lambdaQuery().eq(CfgRuleSalesFormulaEntity::getSalesQtyId, id));
        return list.stream()
                .map(CfgRuleSalesQtyDTO.StrategyFormulaResultDTO::buildFormulaResultDTO).collect(Collectors.toList());
    }

    @Override
    public List<CfgRuleSalesFormulaDTO.SalesFormulaExportDTO> listFormulaByRefIdList(List<String> refIdList) {
        return baseMapper.listFormulaByRefIdList(refIdList);
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<CfgRuleSalesFormulaEntity> newList, List<CfgRuleSalesFormulaEntity> oldList) {
        List<String> newIds = newList.stream().map(CfgRuleSalesFormulaEntity::getId).
                filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(CfgRuleSalesFormulaEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(List<CfgRuleSalesFormulaEntity> list, List<CfgRuleSalesFormulaEntity> oldList, String salesQtyId, Boolean isCustom, String skuType) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        Map<String, List<CfgRuleSalesFormulaEntity>> map = list.stream().collect(Collectors.groupingBy(CfgRuleSalesFormulaEntity::getType));
        for (Map.Entry<String, List<CfgRuleSalesFormulaEntity>> entry : map.entrySet()) {
            List<CfgRuleSalesFormulaEntity> value = entry.getValue();
            //排序
            int maxIndex = (Boolean.TRUE.equals(isCustom)) ? getMaxIndex(oldList, entry) : MathUtil.ZERO;
            validateUniqueNames(value);
            for (CfgRuleSalesFormulaEntity salesFormula : value) {
                salesFormula.setSkuType(skuType);
                updateSalesFormulaAttributes(oldList, salesQtyId, isCustom, salesFormula, maxIndex);
                maxIndex ++;
            }
        }

    }

    /**
     * 更新每条记录的属性
     *
     * @param oldList      旧销量系数
     * @param salesQtyId   销量id
     * @param isCustom     是否自定义
     * @param salesFormula 销量系数
     * @param maxIndex     序号
     */
    private void updateSalesFormulaAttributes(List<CfgRuleSalesFormulaEntity> oldList, String salesQtyId, Boolean isCustom, CfgRuleSalesFormulaEntity salesFormula, int maxIndex) {
        salesFormula.setSalesQtyId(salesQtyId);
        //排序
        salesFormula.setIndex(maxIndex + 1);
        //主键id赋值
        CfgRuleSalesFormulaEntity entity = oldList.stream().filter(obj -> CharSequenceUtil.equals(obj.getType(), salesFormula.getType()) && CharSequenceUtil.equals(obj.getName(), CharSequenceUtil.blankToDefault(salesFormula.getName(),""))).findFirst().orElse(null);
        if (!ObjectUtils.isEmpty(entity)) {
            salesFormula.setId(entity.getId());
            //自定义添加的需要保持原有序号
            salesFormula.setIndex(Boolean.TRUE.equals(isCustom) ? entity.getIndex() : salesFormula.getIndex());
        }
        //固定销量
        if (CfgRuleSalesFormulaDefaultTypeEnum.FIXED.getCode().equals(salesFormula.getDefaultType())) {
            salesFormula.setPercentJson(JSONUtil.parseObj(new CfgRuleSalesFormulaDTO.PercentJsonDTO()));
        }
        //动态销量
        setDynamicFormula(salesFormula);
        //百分比json
        JSONObject percentJson = JSONUtil.parseObj(salesFormula.getPercentJsonDTO());
        salesFormula.setPercentJson(percentJson);

        //时间
        List<LocalDate> dateList = salesFormula.getDateList();
        checkDate(dateList, salesFormula);
    }


    /**
     * 获取最大排序
     *
     * @param oldList 原销量系数
     * @param entry   新销量系数
     */
    private Integer getMaxIndex(List<CfgRuleSalesFormulaEntity> oldList, Map.Entry<String, List<CfgRuleSalesFormulaEntity>> entry) {
        return oldList.stream().filter(obj -> CharSequenceUtil.equals(obj.getType(), entry.getKey())).max(Comparator.comparingInt(CfgRuleSalesFormulaEntity::getIndex)).map(CfgRuleSalesFormulaEntity::getIndex).orElse(MathUtil.ZERO);
    }


    /**
     * 设置动态销量系数
     * @param salesFormula 销量系数
     */
    private void setDynamicFormula(CfgRuleSalesFormulaEntity salesFormula) {
        if (CfgRuleSalesFormulaDefaultTypeEnum.DYNAMIC.getCode().equals(salesFormula.getDefaultType())) {
            salesFormula.setFixedValue(MathUtil.ZERO);
            //默认配置需要校验百分比之和为100
            if (CfgRuleSalesFormulaTypeEnum.DEFAULT.getCode().equals(salesFormula.getType())) {
                Integer totalRatio = salesFormula.getPercentJsonDTO().getTotalRatio();
                if (MathUtil.compareTo(totalRatio,100) != MathUtil.ZERO) {
                    throw new ServiceException("默认动态销量系数之和必须=100%；");
                }
            }
        }
    }

    /**
     * 校验时间
     * @param dateList 时间
     */
    private void checkDate(List<LocalDate> dateList, CfgRuleSalesFormulaEntity salesFormula) {
        if (CollectionUtils.isNotEmpty(dateList)) {
            if (CollectionUtils.isEmpty(dateList) || dateList.size() != 2) {
                throw new ServiceException("时间区间不能为空");
            }
            if (dateList.get(0).isAfter(dateList.get(1))) {
                throw new ServiceException("开始时间不能大于结束时间");
            }
            salesFormula.setStartDate(dateList.get(0));
            salesFormula.setEndDate(dateList.get(1));
        }
    }

    /**
     * 获取销量名称重复数据
     * @param value 参数
     */
    private void validateUniqueNames(List<CfgRuleSalesFormulaEntity> value) {
        String names = value.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getName()))
                .collect(Collectors.groupingBy(CfgRuleSalesFormulaEntity::getName))
                .entrySet().stream().filter(obj -> obj.getValue().size() > MathUtil.ONE).map(Map.Entry::getKey).distinct().collect(Collectors.joining(","));
        if (CharSequenceUtil.isNotBlank(names)) {
            throw new ServiceException("销量名称【{}】唯一不能添加重复数据",names);
        }
    }
}
