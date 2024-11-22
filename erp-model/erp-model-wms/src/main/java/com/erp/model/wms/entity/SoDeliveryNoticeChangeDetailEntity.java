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
 * 发货通知变更单明细
 * </p>
 *
 * @author lrp
 * @since 2024-10-23
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("so_delivery_notice_change_detail")
public class SoDeliveryNoticeChangeDetailEntity extends BaseEntity<SoDeliveryNoticeChangeDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
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
    * 来源明细id
    */
    @TableField("source_detail_id")
    private String sourceDetailId;
    /**
    * 变更类型
    */
    @TableField("change_type")
    private String changeType;
    /**
    * 原发货通知数量
    */
    @TableField("origin_qty")
    private Integer originQty;
    /**
    * 新发货通知数量
    */
    @TableField("new_qty")
    private Integer newQty;
    /**
    * 产品名称
    */
    @TableField("product_name")
    private String productName;
    /**
    * 销售明细id
    */
    @TableField("so_detail_id")
    private String soDetailId;
    /**
     * bom版本
     */
    @TableField("bom_version")
    private String bomVersion;

    /**
     * 客户sku
     */
    @TableField("platform_sku_no")
    private String platformSkuNo;

    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    public static final String CHANGE_TYPE = "change_type";

    public static final String ORIGIN_QTY = "origin_qty";

    public static final String NEW_QTY = "new_qty";

    public static final String PRODUCT_NAME = "product_name";

    public static final String SO_DETAIL_ID = "so_detail_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}