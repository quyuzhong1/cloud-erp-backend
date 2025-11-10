package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 装箱任务明细表
 * </p>
 *
 * @author zdy
 * @since 2024-07-02
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("packing_task_detail")
public class PackingTaskDetailEntity extends BaseEntity<PackingTaskDetailEntity> {

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
    * skuNo
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 发货数量
    */
    @TableField("delivery_qty")
    private Integer deliveryQty;
    /**
    * 来源明细id
    */
    @TableField("source_detail_id")
    private String sourceDetailId;
    /**
    * fnSku
    */
    @TableField("fn_sku")
    private String fnSku;
    /**
     * 三方仓商品条码
     */
    @TableField("third_barcode")
    private String thirdBarcode;

    /**
     * 客户PO号
     */
    @TableField("customer_po")
    private String customerPO;

    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PRODUCT_NAME = "product_name";

    public static final String DELIVERY_QTY = "delivery_qty";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    public static final String FN_SKU = "fn_sku";

    public static final String SOURCE_CODE = "source_code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}