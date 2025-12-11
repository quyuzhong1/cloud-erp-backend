package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * B2B三方发货单明细
 * </p>
 *
 * @author zdy
 * @since 2025-11-26
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("b2b_third_delivery_detail")
public class B2bThirdDeliveryDetailEntity extends BaseEntity<B2bThirdDeliveryDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
     * 销售明细id
     */
    @TableField("so_detail_id")
    private String soDetailId;
    /**
    * 产品id
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * 产品编号
    */
    @TableField("sku_no")
    private String skuNo;
    /**
     * 产品名称
     */
    @TableField("product_name")
    private String productName;
    /**
    * 销售数量
    */
    @TableField("salse_qty")
    private Integer saleQty;
    /**
    * 发货数量
    */
    @TableField("delivery_qty")
    private Integer deliveryQty;
    /**
    * 单箱数量
    */
    @TableField("per_box_qty")
    private Integer perBoxQty;
    /**
    * 发货sku
    */
    @TableField("delivery_sku_no")
    private String deliverySkuNo;
    /**
    * 发货skuid
    */
    @TableField("delivery_sku_id")
    private String deliverySkuId;
    /**
    * 库存sku
    */
    @TableField("warehouse_platform_sku")
    private String warehousePlatformSku;
    /**
    * 发货箱数
    */
    @TableField("box_qty")
    private Integer boxQty;
    /**
    * 规格编号（ZXGG0001）
    */
    @TableField("box_spec_no")
    private String boxSpecNo;
    /**
    * 序号
    */
    @TableField("sort")
    private Integer sort;
    /**
     * 状态(主单)
     */
    @TableField(exist = false)
    private String status;



    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String SALSE_QTY = "salse_qty";

    public static final String DELIVERY_QTY = "delivery_qty";

    public static final String PER_BOX_QTY = "per_box_qty";

    public static final String DELIVERY_SKU_NO = "delivery_sku_no";

    public static final String DELIVERY_SKU_ID = "delivery_sku_id";

    public static final String WAREHOUSE_PLATFORM_SKU = "warehouse_platform_sku";

    public static final String BOX_QTY = "box_qty";

    public static final String BOX_SPEC_NO = "box_spec_no";

    public static final String SORT = "sort";

    @Override
    public Serializable pkVal() {
        return null;
    }

}