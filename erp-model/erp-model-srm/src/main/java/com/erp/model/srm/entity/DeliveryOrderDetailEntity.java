package com.erp.model.srm.entity;

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
 * 送货单明细
 * </p>
 *
 * @author lrp
 * @since 2024-01-12
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("delivery_order_detail")
public class DeliveryOrderDetailEntity extends BaseEntity<DeliveryOrderDetailEntity> {

    /**
    * 送货单Id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 来源id明细
    */
    @TableField("source_detail_id")
    private String sourceDetailId;
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
    * 送货数量
    */
    @TableField("delivery_qty")
    private Integer deliveryQty;
    /**
    * 赠品数量
    */
    @TableField("gift_qty")
    private Integer giftQty;
    /**
    * 收货数量
    */
    @TableField("receive_qty")
    private Integer receiveQty;
    /**
    * 赠品收货数量
    */
    @TableField("gift_receive_qty")
    private Integer giftReceiveQty;
    /**
    * 质检合格数
    */
    @TableField("qc_good_qty")
    private Integer qcGoodQty;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;


    public static final String MAIN_ID = "main_id";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String DELIVERY_QTY = "delivery_qty";

    public static final String GIFT_QTY = "gift_qty";

    public static final String RECEIVE_QTY = "receive_qty";

    public static final String GIFT_RECEIVE_QTY = "gift_receive_qty";

    public static final String QC_GOOD_QTY = "qc_good_qty";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}