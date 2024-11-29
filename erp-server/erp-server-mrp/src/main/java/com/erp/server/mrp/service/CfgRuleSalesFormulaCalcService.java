package com.erp.server.mrp.service;

import com.common.business.service.SuperService;
import com.erp.model.mrp.entity.CfgRuleSalesFormulaCalcEntity;

import java.util.List;

/**
 * <p>
 * 试算销量公式（规则设置） 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
 */
public interface CfgRuleSalesFormulaCalcService extends SuperService<CfgRuleSalesFormulaCalcEntity> {


    /**
     * 根据试算配置id查询销量公式
     *
     * @param cfgRuleCalcId 试算id
     */
    List<CfgRuleSalesFormulaCalcEntity> listByCfgRuleCalcId(String cfgRuleCalcId);

    /**
     * 根据试算配置id查询销量公式
     *
     * @param cfgRuleCalcIds 试算id
     */
    List<CfgRuleSalesFormulaCalcEntity> listByCfgRuleCalcIds(List<String> cfgRuleCalcIds);
}
