package com.erp.model.mrp.entity;

import cn.hutool.json.JSONArray;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 销量试算表
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("calc_sales_info_dim")
public class CalcSalesInfoDimEntity extends BaseEntity<CalcSalesInfoDimEntity> {

    /**
    * skuId
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * sku
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 国家
    */
    @TableField("country")
    private String country;
    /**
    * 店铺
    */
    @TableField("shop_id")
    private String shopId;
    /**
    * 平台
    */
    @TableField("platform")
    private String platform;
    /**
    * 累计销量(去噪后)
    */
    @TableField("sales_qty_json")
    private JSONArray salesQtyJson;
    /**
    * 日均销量(去噪后)
    */
    @TableField("avg_sales_qty_json")
    private JSONArray avgSalesQtyJson;
    /**
    * 预估销量
    */
    @TableField("month_sales_estimate_qty_json")
    private JSONArray monthSalesEstimateQtyJson;
    /**
    * 真实销量
    */
    @TableField("month_real_sales_qty_json")
    private JSONArray monthRealSalesQtyJson;
    /**
    * 试算配置id
    */
    @TableField("cfg_rule_calc_id")
    private String cfgRuleCalcId;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;


    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String COUNTRY = "country";

    public static final String SHOP_ID = "shop_id";

    public static final String PLATFORM = "platform";

    public static final String SALES_QTY_JSON = "sales_qty_json";

    public static final String AVG_SALES_QTY_JSON = "avg_sales_qty_json";

    public static final String MONTH_SALES_ESTIMATE_QTY_JSON = "month_sales_estimate_qty_json";

    public static final String MONTH_REAL_SALES_QTY_JSON = "month_real_sales_qty_json";

    public static final String CFG_RULE_CALC_ID = "cfg_rule_calc_id";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}