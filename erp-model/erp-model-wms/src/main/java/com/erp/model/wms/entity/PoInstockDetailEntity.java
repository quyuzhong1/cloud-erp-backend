package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * <p>
 * 采购入库明细表
 * </p>
 *
 * @author will
 * @since 2023-04-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("po_instock_detail")
public class PoInstockDetailEntity extends BaseEntity<PoInstockDetailEntity> {

    /**
     * 主表id
     */
    @TableField("main_id")
    private String mainId;

    /**
     * skuId
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * sku编码
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * 变体信息
     */
    @TableField("variant_property")
    private String variantProperty;

    /**
     * 入库数量
     */
    @TableField("stock_in_qty")
    private Integer stockInQty;

    /**
     * 采购数量
     */
    @TableField("purchase_qty")
    private Integer purchaseQty;

    /**
     * 超收数量
     */
    @TableField("exceed_qty")
    private Integer exceedQty;

    /**
     * 收货数量
     */
    @TableField("receive_qty")
    private Integer receiveQty;

    /**
     * 仓位
     */
    @TableField("warehouse_location")
    private String warehouseLocation;

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
     * 采购订单明细id
     */
    @TableField("purchase_order_detail_id")
    private String purchaseOrderDetailId;

    /**
     * 金蝶明细id
     */
    @TableField("kingdee_detail_id")
    private String kingdeeDetailId;

    /**
     * 含税单价
     */
    @TableField("tax_price")
    private BigDecimal taxPrice;
    /**
     * 税率
     */
    @TableField("tax_rate")
    private BigDecimal taxRate;
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
     * 是否是自动入库
     */
    @TableField(exist = false)
    private Boolean isAutoInstock;

    @TableField(exist = false)
    private String approveStatus;

    @TableField(exist = false)
    private LocalDate billDate;
}
