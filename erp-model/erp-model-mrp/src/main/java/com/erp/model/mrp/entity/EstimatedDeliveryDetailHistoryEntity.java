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
 * 预计发货明细
 * </p>
 *
 * @author liaohui
 * @since 2024-09-25
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("estimated_delivery_detail_history")
public class EstimatedDeliveryDetailHistoryEntity extends BaseEntity<EstimatedDeliveryDetailHistoryEntity> {

    /**
     * 补货建议id
     */
    @TableField("replenishment_detail_id")
    private String replenishmentDetailId;

    /**
     * 状态
     */
    @TableField("status")
    private String status;

    /**
     * 数量
     */
    @TableField("qty")
    private Integer qty;

    /**
     * 预计到货日期
     */
    @TableField("estimate_sales_date")
    private Date estimateSalesDate;

    /**
     * 业务类型 FBA/海外仓
     */
    @TableField("type")
    private String type;

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
     * 计算版本  所有子表加   根据单号生成规则
     */
    @TableField("calc_version")
    private String calcVersion;


    public static final String REPLENISHMENT_DETAIL_ID = "replenishment_detail_id";

    public static final String STATUS = "status";

    public static final String QTY = "qty";

    public static final String ESTIMATE_SALES_DATE = "estimate_sales_date";

    public static final String TYPE = "type";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String SOURCE_TYPE = "source_type";

    public static final String CALC_VERSION = "calc_version";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
