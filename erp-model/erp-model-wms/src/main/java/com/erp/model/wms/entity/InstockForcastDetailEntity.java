package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 入库预报明细表
 * </p>
 *
 * @author lambda
 * @since 2023-05-09
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("instock_forcast_detail")
public class InstockForcastDetailEntity extends BaseEntity<InstockForcastDetailEntity> {

    /**
     * 入库预报主单id
     */
    @TableField("info_id")
    private String infoId;

    /**
     * 采购订单明细id
     */
    @TableField("purchase_order_detail_id")
    private String purchaseOrderDetailId;

    /**
     * sku id
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * sku编号
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * 数量
     */
    @TableField("qty")
    private Integer qty;

    /**
     * 产品名称
     */
    @TableField("product_name")
    private String productName;


    public static final String INFO_ID = "info_id";

    public static final String PURCHASE_ORDER_DETAIL_ID = "purchase_order_detail_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PRODUCT_NAME = "product_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
