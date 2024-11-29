package com.erp.server.mrp.service.impl;


import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.mrp.dto.CfgRuleSalesFormulaDTO;
import com.erp.model.mrp.entity.CalcSalesInfoEstimateEntity;
import com.erp.model.mrp.entity.CfgRuleSalesFormulaCalcEntity;
import com.erp.server.mrp.mapper.CalcSalesInfoEstimateMapper;
import com.erp.server.mrp.service.CalcSalesInfoEstimateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
/**
 * <p>
 * 试算销量预估 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
 */
@Slf4j
@Service
public class CalcSalesInfoEstimateServiceImpl extends SuperServiceImpl<CalcSalesInfoEstimateMapper, CalcSalesInfoEstimateEntity> implements CalcSalesInfoEstimateService {


    @Override
    public List<CalcSalesInfoEstimateEntity> listByCalcSalesInfoIds(List<String> ids) {
        List<CalcSalesInfoEstimateEntity> list = list(Wrappers.<CalcSalesInfoEstimateEntity>lambdaQuery()
                .in(CalcSalesInfoEstimateEntity::getCalcSalesInfoDimId, ids));

        for (CalcSalesInfoEstimateEntity formulaEntity : list) {
            //百分比json
            CfgRuleSalesFormulaDTO.PercentJsonDTO percentJsonDTO = JSONUtil.toBean(formulaEntity.getPercentJson(), CfgRuleSalesFormulaDTO.PercentJsonDTO.class);
            formulaEntity.setPercentJsonDTO(percentJsonDTO);
        }
        return list;
    }
}
