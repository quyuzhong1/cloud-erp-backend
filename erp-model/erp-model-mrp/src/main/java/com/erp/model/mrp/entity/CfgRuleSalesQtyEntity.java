package com.erp.model.mrp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 销量（规则设置）
 * </p>
 *
 * @author will
 * @since 2024-08-23
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_rule_sales_qty")
public class CfgRuleSalesQtyEntity extends BaseEntity<CfgRuleSalesQtyEntity> {

    /**
    * 是否同常规品配置一致,true是，false否
    */
    @TableField("is_cfg_same")
    private Boolean isCfgSame;
    /**
    * 断货数据是否从历史销量中排除,true是，false否
    */
    @TableField("is_ignore_out_of_stock")
    private Boolean isIgnoreOutOfStock;
    /**
    * sales_qty_type
    * 销量计算类型，byCreateTime以销售订单订单创建时间计算销量，byOutStockTime以销售出库单出库时间计算销量
    */
    @TableField("sales_qty_type")
    private String salesQtyType;
    /**
    * 订单类型，all:全部，fba:FBA,fbm:FBM
    */
    @TableField("order_type")
    private String orderType;
    /**
    * 平台类型(amazon Amazon、overseas 海外、internal 国内、b2b B2B)
    */
    @TableField("platform_type")
    private String platformType;
    /**
    * 关联id
    */
    @TableField("ref_id")
    private String refId;
    /**
    * 关联类型
    */
    @TableField("ref_type")
    private String refType;
    /**
    * 类型，new 新品、conventional常规品
    */
    @TableField("type")
    private String type;


    public static final String IS_CFG_SAME = "is_cfg_same";

    public static final String IS_IGNORE_OUT_OF_STOCK = "is_ignore_out_of_stock";

    public static final String SALES_QTY_TYPE = "sales_qty_type";

    public static final String ORDER_TYPE = "order_type";

    public static final String PLATFORM_TYPE = "platform_type";

    public static final String REF_ID = "ref_id";

    public static final String REF_TYPE = "ref_type";

    public static final String TYPE = "type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}