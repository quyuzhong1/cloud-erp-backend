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
 * 本地在途明细
 * </p>
 *
 * @author liaohui
 * @since 2024-09-25
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("local_in_transit_detail_history")
public class LocalInTransitDetailHistoryEntity extends BaseEntity<LocalInTransitDetailHistoryEntity> {

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
    private Date planArrivalDate;

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

    /**
     * 预计可售日期
     */
    @TableField("estimate_sales_date")
    private Date estimateSalesDate;


    public static final String REPLENISHMENT_DETAIL_ID = "replenishment_detail_id";

    public static final String QTY = "qty";

    public static final String PLAN_ARRIVAL_DATE = "plan_arrival_date";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String CALC_VERSION = "calc_version";

    public static final String SOURCE_TYPE = "source_type";

    public static final String ESTIMATE_SALES_DATE = "estimate_sales_date";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
