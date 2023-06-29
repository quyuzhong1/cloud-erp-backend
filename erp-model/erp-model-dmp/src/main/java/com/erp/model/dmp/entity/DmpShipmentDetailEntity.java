package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 调拨发货明细
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-06-29
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_shipment_detail")
public class DmpShipmentDetailEntity extends BaseEntity<DmpShipmentDetailEntity> {


    /**
    * 主单id
    */
    @TableField("main_id")
    private String mainId;

    /**
    * 平台sku
    */
    @TableField("platform_sku")
    private String platformSku;

    /**
    * 申报返回数量
    */
    @TableField("apply_qty")
    private Integer applyQty;

    /**
    * 发货数量
    */
    @TableField("delivery_qty")
    private Integer deliveryQty;

    /**
    * 签收数量
    */
    @TableField("receive_qty")
    private Integer receiveQty;

    /**
    * 产品asin
    */
    @TableField("asin")
    private String asin;

    /**
    * 本地库存类型
    */
    @TableField("stock_type")
    private String stockType;

    /**
    * 本地库存sku
    */
    @TableField("sku_no")
    private String skuNo;

    /**
    * SKU图片
    */
    @TableField("picture_url")
    private String pictureUrl;

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
    * FNSKU
    */
    @TableField("fn_sku")
    private String fnSku;


    public static final String MAIN_ID = "main_id";

    public static final String PLATFORM_SKU = "platform_sku";

    public static final String APPLY_QTY = "apply_qty";

    public static final String DELIVERY_QTY = "delivery_qty";

    public static final String RECEIVE_QTY = "receive_qty";

    public static final String ASIN = "asin";

    public static final String STOCK_TYPE = "stock_type";

    public static final String STOCK_SKU = "stock_sku";

    public static final String PICTURE_URL = "picture_url";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String FN_SKU = "fn_sku";

    @Override
    public Serializable pkVal() {
        return null;
    }

}