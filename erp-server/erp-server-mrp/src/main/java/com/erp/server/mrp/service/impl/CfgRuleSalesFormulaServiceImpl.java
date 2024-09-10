package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.ObjectUtil;
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
import com.erp.model.mrp.enums.CfgRuleSalesFormulaDefaultTypeEnum;
import com.erp.model.mrp.enums.CfgRuleSalesFormulaTypeEnum;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
        //自定义更新无需删除
        if (!isCustom) {
            //原物流信息
            List<CfgRuleSalesFormulaEntity> oldList = listBySalesQtyIdList(Arrays.asList(salesQtyId));
            //删除明细
            List<String> deleteIds = getDeleteIds(list, oldList);
            if (CollectionUtils.isNotEmpty(deleteIds)) {
                this.removeByIds(deleteIds);
            }
        }
        if (CollectionUtils.isEmpty(list)) {
            return  Boolean.TRUE;
        }
        // 数据处理
        handleData(list,salesQtyId);
        log.info("编辑 开始修改销量公式（规则设置）数据，id：【{}】", salesQtyId);
        boolean save = super.saveOrUpdateBatch(list);
        if(!save) {
            throw new ServiceException("销量公式（规则设置）保存失败");
        }
        return Boolean.TRUE;
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
                .map(v -> {
                    CfgRuleSalesQtyDTO.StrategyFormulaResultDTO resultDTO = BeanMapperUtils.map(CfgRuleSalesQtyDTO.StrategyFormulaResultDTO.class, v);
                    //百分比json
                    CfgRuleSalesFormulaDTO.PercentJsonDTO percentJsonDTO = JSONUtil.toBean(resultDTO.getPercentJson(), CfgRuleSalesFormulaDTO.PercentJsonDTO.class);
                    resultDTO.setPercentJsonDTO(percentJsonDTO);
                    return resultDTO;
                }).collect(Collectors.toList());
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
    private void handleData(List<CfgRuleSalesFormulaEntity> list,String salesQtyId) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        //销量信息
        CfgRuleSalesQtyEntity salesQtyEntity = cfgRuleSalesQtyService.getById(salesQtyId);
        if (ObjectUtil.isEmpty(salesQtyEntity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"规则设置（销量）");
        }

        Integer index = MathUtil.ONE;
        for (CfgRuleSalesFormulaEntity salesFormula : list) {
            salesFormula.setSalesQtyId(salesQtyId);
            //排序
            salesFormula.setIndex(index);
            //固定销量
            if (CfgRuleSalesFormulaDefaultTypeEnum.FIXED.getCode().equals(salesFormula.getDefaultType())) {
                salesFormula.setPercentJson(JSONUtil.parseObj(new CfgRuleSalesFormulaDTO.PercentJsonDTO()));
            }
            //动态销量
            if (CfgRuleSalesFormulaDefaultTypeEnum.DYNAMIC.getCode().equals(salesFormula.getDefaultType())) {
                salesFormula.setFixedValue(BigDecimal.ZERO);
                //默认配置需要校验百分比之和为100
                if (CfgRuleSalesFormulaTypeEnum.DEFAULT.getCode().equals(salesFormula.getType())) {
                    BigDecimal totalRatio = salesFormula.getPercentJsonDTO().getTotalRatio();
                    if (MathUtil.compareTo(totalRatio,new BigDecimal(100)) != MathUtil.ZERO) {
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
            index ++;
        }
    }
}
