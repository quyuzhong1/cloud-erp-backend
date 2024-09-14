package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
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
import com.erp.server.mrp.service.CfgRuleSalesQtyService;
import com.erp.server.mrp.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private CfgRuleSalesQtyService cfgRuleSalesQtyService;

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<CfgRuleSalesFormulaDTO.UpdateDTO> salesFormulaList,String salesQtyId,Boolean isCustom) {
        if (CollectionUtils.isEmpty(salesFormulaList)) {
            salesFormulaList = Collections.EMPTY_LIST;
        }
        List<CfgRuleSalesFormulaEntity> list = BeanMapperUtils.copyList(CfgRuleSalesFormulaEntity.class, salesFormulaList);

        //原物流信息
        List<CfgRuleSalesFormulaEntity> oldList = listBySalesQtyIdList(Arrays.asList(salesQtyId));
        //自定义更新无需删除
        if (!isCustom) {
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
        //销量信息
        CfgRuleSalesQtyEntity salesQtyEntity = cfgRuleSalesQtyService.getById(salesQtyId);
        if (ObjectUtil.isEmpty(salesQtyEntity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"规则设置（销量）");
        }
        // 数据处理
        handleData(list,oldList,salesQtyId,isCustom);
        log.info("编辑 开始修改销量公式（规则设置）数据，id：【{}】", salesQtyId);
        boolean save = super.saveOrUpdateBatch(list);
        if(!save) {
            throw new ServiceException("销量公式（规则设置）保存失败");
        }
        addOperateLog(list,salesQtyEntity);
        return Boolean.TRUE;
    }
    /**
     * 添加日志
     * @author will
     * @date 2024/9/12 10:26
     * @param list
     * @param salesQtyEntity
     */
    private void addOperateLog (List<CfgRuleSalesFormulaEntity> list,CfgRuleSalesQtyEntity salesQtyEntity) {
        Map<String, List<CfgRuleSalesFormulaEntity>> map = list.stream().collect(Collectors.groupingBy(obj -> obj.getType().concat(StrUtil.blankToDefault(obj.getDefaultType(),""))));
        //日志
        for (Map.Entry<String, List<CfgRuleSalesFormulaEntity>> entry : map.entrySet()) {
            List<CfgRuleSalesFormulaEntity> value = entry.getValue();
            StringBuffer msg = new StringBuffer();
            msg.append(StrUtil.format("{}_{}日销量：<br>",CfgRuleStockingRatioTypeEnum.getName(salesQtyEntity.getType()),CfgRuleSalesFormulaTypeEnum.getName(value.get(0).getType())));
            for (CfgRuleSalesFormulaEntity formulaEntity : value) {
                if (!StrUtil.equals(formulaEntity.getType(),CfgRuleSalesFormulaTypeEnum.DEFAULT.getCode()) ) {
                    msg.append(StrUtil.format("•序号【{}】、名称【{}】、时间段【{}】<br>" ,formulaEntity.getIndex(),formulaEntity.getName(),StrUtil.format("{}~{}",formulaEntity.getStartDate(),formulaEntity.getEndDate())));
                }
                JSONObject percentJson = formulaEntity.getPercentJson();
                percentJson.entrySet().stream().forEach(obj -> {
                    msg.append(StrUtil.format("•{}：{}<br>", CfgRulePercentEnum.getName(obj.getKey()),obj.getValue()));
                });
            }
            operateLogService.addModuleOperateLog(msg.toString(), ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), StrUtil.blankToDefault(salesQtyEntity.getRefId(),salesQtyEntity.getId()), "销量");
        }
    }

    @Override
    public List<CfgRuleSalesFormulaEntity> listBySalesQtyIdList(List<String> salesQtyIdList) {
        if (CollectionUtils.isEmpty(salesQtyIdList)) {
            return  Collections.EMPTY_LIST;
        }
        List<CfgRuleSalesFormulaEntity> list = lambdaQuery().in(CfgRuleSalesFormulaEntity::getSalesQtyId, salesQtyIdList).list();
        if (CollectionUtils.isEmpty(list)) {
            return  Collections.EMPTY_LIST;
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
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(CfgRuleSalesFormulaEntity::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(CfgRuleSalesFormulaEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(List<CfgRuleSalesFormulaEntity> list,List<CfgRuleSalesFormulaEntity> oldList,String salesQtyId,Boolean isCustom) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        Map<String, List<CfgRuleSalesFormulaEntity>> map = list.stream().collect(Collectors.groupingBy(CfgRuleSalesFormulaEntity::getType));
        for (Map.Entry<String, List<CfgRuleSalesFormulaEntity>> entry : map.entrySet()) {
            List<CfgRuleSalesFormulaEntity> value = entry.getValue();
            //排序
            Integer maxIndex = MathUtil.ZERO;
            if (isCustom) {
                maxIndex = oldList.stream().filter(obj -> StrUtil.equals(obj.getType(),entry.getKey())).max(Comparator.comparingInt(CfgRuleSalesFormulaEntity::getIndex)).map(CfgRuleSalesFormulaEntity::getIndex).orElse(MathUtil.ZERO);
            }
            String names = value.stream().filter(obj -> StrUtil.isNotBlank(obj.getName())).collect(Collectors.groupingBy(CfgRuleSalesFormulaEntity::getName)).entrySet().stream().filter(obj -> obj.getValue().size() > MathUtil.ONE).map(obj -> obj.getKey()).distinct().collect(Collectors.joining(","));
            if (StrUtil.isNotBlank(names)) {
                throw new ServiceException("销量名称【{}】唯一不能添加重复数据",names);
            }

            for (CfgRuleSalesFormulaEntity salesFormula : value) {
                salesFormula.setSalesQtyId(salesQtyId);
                //排序
                salesFormula.setIndex(maxIndex + 1);
                //主键id赋值
                CfgRuleSalesFormulaEntity entity = oldList.stream().filter(obj -> StrUtil.equals(obj.getType(),salesFormula.getType()) && StrUtil.equals(obj.getName(), StrUtil.blankToDefault(salesFormula.getName(),""))).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(entity)) {
                    salesFormula.setId(entity.getId());
                    //自定义添加的需要保持原有序号
                    salesFormula.setIndex(isCustom ? entity.getIndex() : salesFormula.getIndex());
                }

                //固定销量
                if (CfgRuleSalesFormulaDefaultTypeEnum.FIXED.getCode().equals(salesFormula.getDefaultType())) {
                    salesFormula.setPercentJson(JSONUtil.parseObj(new CfgRuleSalesFormulaDTO.PercentJsonDTO()));
                }
                //动态销量
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
                //百分比json
                JSONObject percentJson = JSONUtil.parseObj(salesFormula.getPercentJsonDTO());
                salesFormula.setPercentJson(percentJson);

                //时间
                List<LocalDate> dateList = salesFormula.getDateList();
                if (CollectionUtils.isNotEmpty(dateList)) {
                    if (CollectionUtils.isEmpty(dateList) || dateList.size() != 2) {
                        throw new ServiceException("时间区间不能为空");
                    }
                    if (dateList.get(0).isAfter(dateList.get(1))) {
                        throw new ServiceException("开始时间不能大于结束时间");
                    }
                }
                salesFormula.setStartDate(CollectionUtils.isNotEmpty(dateList) ? dateList.get(0) : null);
                salesFormula.setEndDate(CollectionUtils.isNotEmpty(dateList) ? dateList.get(1) : null);
                maxIndex ++;
            }
        }

    }
}
