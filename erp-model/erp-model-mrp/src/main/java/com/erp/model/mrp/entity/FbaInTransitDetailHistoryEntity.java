package com.erp.model.mrp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * fba在途明细
 * </p>
 *
 * @author liaohui
 * @since 2024-09-25
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("fba_in_transit_detail_history")
public class FbaInTransitDetailHistoryEntity extends BaseEntity<FbaInTransitDetailHistoryEntity> {

    /**
     * 补货建议id
     */
    @TableField("replenishment_detail_id")
    private String replenishmentDetailId;

    /**
     * 来源id
     */
    @TableField("source_id")
    private String sourceId;

    /**
     * 来源单号
     */
    @TableField("source_code")
    private String sourceCode;

    /**
     * 来源类型
     */
    @TableField("source_type")
    private String sourceType;

    /**
     * 发货状态
     */
    @TableField("status")
    private String status;

    /**
     * 发货日期
     */
    @TableField("delivery_date")
    private Date deliveryDate;

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
     * 签收数量
     */
    @TableField("receive_qty")
    private Integer receiveQty;

    /**
     * 在途
     */
    @TableField("in_transit_qty")
    private Integer inTransitQty;

    /**
     * 预计可售日期
     */
    @TableField("estimate_sales_date")
    private Date estimateSalesDate;

    /**
     * 计算版本  所有子表加   根据单号生成规则
     */
    @TableField("calc_version")
    private String calcVersion;


    public static final String REPLENISHMENT_DETAIL_ID = "replenishment_detail_id";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String SOURCE_TYPE = "source_type";

    public static final String STATUS = "status";

    public static final String DELIVERY_DATE = "delivery_date";

    public static final String DECLARE_QTY = "declare_qty";

    public static final String DELIVERY_QTY = "delivery_qty";

    public static final String RECEIVE_QTY = "receive_qty";

    public static final String IN_TRANSIT_QTY = "in_transit_qty";

    public static final String ESTIMATE_SALES_DATE = "estimate_sales_date";

    public static final String CALC_VERSION = "calc_version";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
