package com.erp.model.bi.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Lambda
 * @Classname DataSourceCostEnum
 * @Description 数据源 枚举
 * @Date 2023-09-20 9:40
 * @Created by yl
 */
public enum DataSourceCostEnum  implements EnumMessage {
    COST_SALELABORCOST("cost_saleLaborCost", "销售人工成本"),
    COST_SHAREDEXPENSES_FBA("cost_sharedExpenses_fba", "公摊费用[FBA部门]"),
    COST_SALESPROFIT("cost_salesProfit", "销售利润额"),
    COST_MAINBUSINESSINCOME("cost_mainBusinessIncome", "主营业务收入"),
    COST_TOTALCOST("cost_totalCost", "成本合计"),
    COST_SALEEXPENSES("cost_saleExpenses", "销售费用小计"),
    ;
    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    public String code;
    /**
     * 名称
     */
    private String name;

    DataSourceCostEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }
}
