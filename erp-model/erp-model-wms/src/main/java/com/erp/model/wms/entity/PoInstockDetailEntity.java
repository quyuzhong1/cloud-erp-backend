package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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

    @TableField(exist = false)
    private String approveStatus;

    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PRODUCT_NAME = "product_name";

    public static final String VARIANT_PROPERTY = "variant_property";

    public static final String STOCK_IN_QTY = "stock_in_qty";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    public static final String PURCHASE_QTY = "purchase_qty";

    public static final String EXCEED_QTY = "exceed_qty";

    public static final String WAREHOUSE_LOCATION = "warehouse_location";

    public static final String REMARK = "remark";

}
