package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 销售订单出库明细
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("so_outstock_detail")
public class SoOutstockDetailEntity extends BaseEntity<SoOutstockDetailEntity> {

    /**
     * 主表id
     */
    @TableField("main_id")
    private String mainId;

    /**
     * sku id
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * sku no
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * 应发数量
     */
    @TableField("plan_qty")
    private Integer planQty;

    /**
     * 实发数量
     */
    @TableField("actual_qty")
    private Integer actualQty;

    /**
     * 库位
     */
    @TableField("warehouse_location")
    private String warehouseLocation;

    /**
     * 虚拟仓库Id
     */
    @TableField("virtual_warehouse_id")
    private String virtualWarehouseId;

    /**
     * 仓库id
     */
    @TableField("warehouse_id")
    private String warehouseId;

    /**
     * 仓库名称
     */
    @TableField("warehouse_name")
    private String warehouseName;


    /**
     * 单价
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 税率
     */
    @TableField("tax_rate")
    private BigDecimal taxRate;

    /**
     * 销售金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 币种
     */
    @TableField("currency")
    private String currency;

    /**
     * 币种符号
     */
    @TableField("currency_symbol")
    private String currencySymbol;


    /**
     * 价税合计(本位币)
     */
    @TableField("all_amount_local_currency")
    private BigDecimal allAmountLocalCurrency;

    /**
     * 汇率
     */
    @TableField(value = "exchange_rate")
    private BigDecimal exchangeRate;

    /**
     * 销售明细id
     */
    @TableField(value = "so_detail_id")
    private String soDetailId;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 来源明细id
     */
    @TableField("source_detail_id")
    private String sourceDetailId;
    
    /**
     * 来源明细id
     */
    @TableField("platform_code")
    private String platformCode;

    /**
     * 平台销售出库单明细ID
     */
    @TableField("platform_detail_id")
    private String platformDetailId;

    @TableField(exist = false)
    private String approveStatus;

    @TableField(exist = false)
    private Boolean invalidStatus ;

    @TableField(exist = false)
    private String soId ;

    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PLAN_QTY = "plan_qty";

    public static final String ACTUAL_QTY = "actual_qty";

    public static final String WAREHOUSE_LOCATION = "warehouse_location";

    public static final String IS_CLOSE = "is_close";

    

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
