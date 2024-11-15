package com.erp.model.mrp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * <p>
 * 本地在途明细
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("local_in_transit_detail")
@EqualsAndHashCode(callSuper = true)
public class LocalInTransitDetailEntity extends BaseEntity<LocalInTransitDetailEntity> {

    private static final long serialVersionUID = -2791590687531003508L;
    /**
     * 补货建议id
     */
    @TableField("replenishment_detail_id")
    private String replenishmentDetailId;

    /**
     * 数量
     */
    @TableField("qty")
    private Integer qty;

    /**
     * 预计到货日期
     */
    @TableField("plan_arrival_date")
    private LocalDate planArrivalDate;

    /**
     * 预计可售日期
     */
    @TableField("estimate_sales_date")
    private LocalDate estimateSalesDate;

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
     * 计算版本  所有子表加   根据单号生成规则
     */
    @TableField("calc_version")
    private String calcVersion;

    /**
     * 来源类型
     */
    @TableField("source_type")
    private String sourceType;


    public static final String REPLENISHMENT_DETAIL_ID = "replenishment_detail_id";

    public static final String QTY = "qty";

    public static final String PLAN_ARRIVAL_DATE = "plan_arrival_date";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String CALC_VERSION = "calc_version";

    public static final String SOURCE_TYPE = "source_type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
