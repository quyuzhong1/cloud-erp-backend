package com.erp.server.mrp.service.impl;


import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.mrp.dto.CfgRuleSalesFormulaDTO;
import com.erp.model.mrp.entity.CfgRuleSalesFormulaEntity;
import com.erp.server.mrp.mapper.CfgRuleSalesFormulaMapper;
import com.erp.server.mrp.service.CfgRuleSalesFormulaService;
import com.erp.server.mrp.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<CfgRuleSalesFormulaDTO.UpdateDTO> salesFormulaList,String salesQtyId) {
        if (CollectionUtils.isEmpty(salesFormulaList)) {
            salesFormulaList = Collections.EMPTY_LIST;
        }
        List<CfgRuleSalesFormulaEntity> list = BeanMapperUtils.copyList(CfgRuleSalesFormulaEntity.class, salesFormulaList);
        //原物流信息
        List<CfgRuleSalesFormulaEntity> oldList = listBySalesQtyIdList(Arrays.asList(salesQtyId));

        //删除明细
        List<String> deleteIds = getDeleteIds(list, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            this.removeByIds(deleteIds);
        }
        // 数据处理
        handleData(list);
        log.info("编辑 开始修改销量公式（规则设置）数据，id：【{}】", salesQtyId);
        boolean save = super.updateBatchById(list);
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
    private void handleData(List<CfgRuleSalesFormulaEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (CfgRuleSalesFormulaEntity salesFormula : list) {
            //百分比json
            JSONObject percentJson = JSONUtil.parseObj(salesFormula.getPercentJsonDTO());
            salesFormula.setPercentJson(percentJson);

            //时间
            List<LocalDate> dateList = salesFormula.getDateList();
            salesFormula.setStartDate(CollectionUtils.isNotEmpty(dateList) ? dateList.get(0) : null);
            salesFormula.setEndDate(CollectionUtils.isNotEmpty(dateList) ? dateList.get(1) : null);
        }
    }
}
