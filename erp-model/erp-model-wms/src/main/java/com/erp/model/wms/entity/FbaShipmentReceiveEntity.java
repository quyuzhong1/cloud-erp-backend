package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * FBA货件签收信息
 * </p>
 *
 * @author Jim
 * @since 2023-11-01
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("fba_shipment_receive")
public class FbaShipmentReceiveEntity extends BaseEntity<FbaShipmentReceiveEntity> {

    /**
    * FBA拣货明细表id
    */
    @TableField("detail_id")
    private String detailId;
    /**
    * 平台sku
    */
    @TableField("asin")
    private String asin;
    /**
    * 卖家sku
    */
    @TableField("msku")
    private String msku;
    /**
    * FNSKU
    */
    @TableField("fn_sku")
    private String fnSku;
    /**
    * ERP的SKU
    */
    @TableField("sku_no")
    private String skuNo;
    /**
     * ERP的SKU ID
     */
    @TableField("sku_id")
    private String skuId;
    /**
    * 申报数量
    */
    @TableField("declare_qty")
    private Integer declareQty;
    /**
    * 发货数量
    */
    @TableField("delivery_qty")
    private Integer deliveryQty;
    /**
    * 收发差异
    */
    @TableField("diff_qty")
    private Integer diffQty;
    /**
    * 收货数量
    */
    @TableField("receive_qty")
    private Integer receiveQty;
    /**
    * 最新签收日期
    */
    @TableField("receive_date")
    private LocalDateTime receiveDate;


    public static final String DETAIL_ID = "detail_id";

    public static final String FBA_SHIPMENT_ID = "fba_shipment_id";

    public static final String ASIN = "asin";

    public static final String M_SKU = "m_sku";

    public static final String FN_SKU = "fn_sku";

    public static final String SKU_NO = "sku_no";

    public static final String PRODUCT_NAME = "product_name";

    public static final String DECLARE_QTY = "declare_qty";

    public static final String DELIVERY_QTY = "delivery_qty";

    public static final String DIFF_QTY = "diff_qty";

    public static final String IS_COMBO = "is_combo";

    public static final String RECEIVE_QTY = "receive_qty";

    public static final String RECEIVE_DATE = "receive_date";


}