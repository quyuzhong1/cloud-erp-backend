package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 计算量明细
 * </p>
 *
 * @author liaohui
 * @since 2024-12-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("mould_ref_calc_qty_detail")
public class MouldRefCalcQtyDetailEntity extends BaseEntity<MouldRefCalcQtyDetailEntity> {

    /**
     * 主表id
     */
    @TableField("main_id")
    private String mainId;

    /**
     * 采购数量
     */
    @TableField("purchase_qty")
    private Integer purchaseQty;

    /**
     * 收获数量
     */
    @TableField("receive_qty")
    private Integer receiveQty;

    /**
     * 入库数量
     */
    @TableField("stock_in_qty")
    private Integer stockInQty;

    /**
     * 采购单号
     */
    @TableField("purchase_order_code")
    private String purchaseOrderCode;

    /**
     * 创建时间
     */
    @TableField("po_create_time")
    private Date poCreateTime;

    /**
     * skuId
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * skuNo
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * 供应商
     */
    @TableField("supplier_id")
    private String supplierId;


    public static final String MAIN_ID = "main_id";

    public static final String PURCHASE_QTY = "purchase_qty";

    public static final String RECEIVE_QTY = "receive_qty";

    public static final String STOCK_IN_QTY = "stock_in_qty";

    public static final String PURCHASE_ORDER_CODE = "purchase_order_code";

    public static final String PO_CREATE_TIME = "po_create_time";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String SUPPLIER_ID = "supplier_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
