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
 * 发货通知单主表明细表
 * </p>
 *
 * @author LUO_WG
 * @since 2023-05-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("so_delivery_notice_detail")
public class SoDeliveryNoticeDetailEntity extends BaseEntity<SoDeliveryNoticeDetailEntity> {

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
     * sku编号
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * 发货数量
     */
    @TableField("delivery_qty")
    private Integer deliveryQty;

    /**
     * 是否关闭
     */
    @TableField("is_close")
    private Boolean isClose;

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
     * 单据状态
     */
    private String approveStatus;


    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String DELIVERY_QTY = "delivery_qty";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String IS_CLOSE = "is_close";

    public static final String REMARK = "remark";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
