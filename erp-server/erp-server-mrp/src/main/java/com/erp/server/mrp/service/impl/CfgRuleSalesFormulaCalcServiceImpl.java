package com.erp.server.mrp.service.impl;


import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.mrp.dto.CfgRuleSalesFormulaDTO;
import com.erp.model.mrp.entity.CfgRuleSalesFormulaCalcEntity;
import com.erp.server.mrp.mapper.CfgRuleSalesFormulaCalcMapper;
import com.erp.server.mrp.service.CfgRuleSalesFormulaCalcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 试算销量公式（规则设置） 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
 */
@Slf4j
@Service
public class CfgRuleSalesFormulaCalcServiceImpl extends SuperServiceImpl<CfgRuleSalesFormulaCalcMapper, CfgRuleSalesFormulaCalcEntity> implements CfgRuleSalesFormulaCalcService {

    @Override
    public List<CfgRuleSalesFormulaCalcEntity> listByCfgRuleCalcId(String id) {
        List<CfgRuleSalesFormulaCalcEntity> list = list(Wrappers.<CfgRuleSalesFormulaCalcEntity>lambdaQuery()
                .eq(CfgRuleSalesFormulaCalcEntity::getCfgRuleCalcId, id)
                .orderByAsc(CfgRuleSalesFormulaCalcEntity::getIndex));

        for (CfgRuleSalesFormulaCalcEntity formulaEntity : list) {
            //百分比json
            CfgRuleSalesFormulaDTO.PercentJsonDTO percentJsonDTO = JSONUtil.toBean(formulaEntity.getPercentJson(), CfgRuleSalesFormulaDTO.PercentJsonDTO.class);
            formulaEntity.setPercentJsonDTO(percentJsonDTO);
            //时间
            formulaEntity.setDateList(Arrays.asList(formulaEntity.getStartDate(),formulaEntity.getEndDate()));
        }
        return list;
    }
}
