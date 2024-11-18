package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.erp.model.scm.dto.SubcontractChangeDetailDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


/**
 * <p>
 * 委外变单明细
 * </p>
 *
 * @author will
 * @since 2023-06-08
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("subcontract_change_detail")
public class SubcontractChangeDetailEntity extends BaseEntity<SubcontractChangeDetailEntity> {


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
    * 变更类型(操作类型)
    */
    @TableField("opt_type")
    private String optType;

    /**
    * 采购数量
    */
    @TableField("qty")
    private Integer qty;

    /**
    * 含税单价
    */
    @TableField("price")
    private BigDecimal price;

    /**
     * 采购金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
    * 币别
    */
    @TableField("currency")
    private String currency;

    /**
    * 币种符号
    */
    @TableField("currency_symbol")
    private String currencySymbol;

    /**
    * 领料数量(发料数量)
    */
    @TableField("delivery_qty")
    private Integer deliveryQty;

    /**
    * 原采购数量
    */
    @TableField("old_qty")
    private Integer oldQty;

    /**
    * 原领料数量(发料数量)
    */
    @TableField("old_delivery_qty")
    private Integer oldDeliveryQty;

    /**
    * 原含税单价
    */
    @TableField("old_price")
    private BigDecimal oldPrice;

    /**
    * 原采购金额
    */
    @TableField("old_amount")
    private BigDecimal oldAmount;

    /**
    * 供应商id
    */
    @TableField("supplier_id")
    private String supplierId;

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
     * 仓位
     */
    @TableField("warehouse_location")
    private String warehouseLocation;

    /**
    * bom版本
    */
    @TableField("bom_version")
    private String bomVersion;

    /**
    * 变更备注
    */
    @TableField("remark")
    private String remark;

    /**
    * 来源明细id
    */
    @TableField("source_detail_id")
    private String sourceDetailId;

    /**
    * 父级SKUid
    */
    @TableField("parent_id")
    private String parentId;

    /**
    * 是否自动生成采购订单
    */
    @TableField("is_generate_po")
    private Boolean isGeneratePo;

    /**
     * 预计交货日期
     */
    @TableField("plan_delivery_date")
    private LocalDate planDeliveryDate;

    /**
     * 付款条件
     */
    @TableField("payment_condition")
    private String paymentCondition;

    @TableField(exist = false)
    private Boolean isAdd;

    @TableField(exist = false)
    private String supplierName;

    @TableField(exist = false)
    private List<SubcontractChangeDetailDTO.UpdateDTO> childList;


    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String VARIANT_PROPERTY = "variant_property";

    public static final String OPT_TYPE = "opt_type";

    public static final String FIELD_QTY = "qty";

    public static final String FIELD_PRICE = "price";

    public static final String FIELD_CURRENCY = "currency";

    public static final String CURRENCY_SYMBOL = "currency_symbol";

    public static final String DELIVERY_QTY = "delivery_qty";

    public static final String OLD_QTY = "old_qty";

    public static final String OLD_DELIVERY_QTY = "old_delivery_qty";

    public static final String OLD_PRICE = "old_price";

    public static final String FIELD_AMOUNT = "amount";

    public static final String OLD_AMOUNT = "old_amount";

    public static final String SUPPLIER_ID = "supplier_id";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String BOM_VERSION = "bom_version";

    public static final String FIELD_REMARK = "remark";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    public static final String PARENT_ID = "parent_id";

    public static final String IS_GENERATE_PO = "is_generate_po";

    @Override
    public Serializable pkVal() {
        return null;
    }

}