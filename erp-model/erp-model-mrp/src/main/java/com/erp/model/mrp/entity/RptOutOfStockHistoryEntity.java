package com.erp.model.mrp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 断货报告
 * </p>
 *
 * @author liaohui
 * @since 2024-09-25
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("rpt_out_of_stock_history")
public class RptOutOfStockHistoryEntity extends BaseEntity<RptOutOfStockHistoryEntity> {

    /**
     * 补货建议id
     */
    @TableField("replenishment_detail_id")
    private String replenishmentDetailId;

    /**
     * 日期
     */
    @TableField("date")
    private Date date;

    /**
     * 断货开始日期
     */
    @TableField("start_date")
    private Date startDate;

    /**
     * 断货结束日期
     */
    @TableField("end_date")
    private Date endDate;

    /**
     * 销量
     */
    @TableField("sales_qty")
    private BigDecimal salesQty;

    /**
     * 金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 计算版本  所有子表加   根据单号生成规则
     */
    @TableField("calc_version")
    private String calcVersion;


    public static final String REPLENISHMENT_DETAIL_ID = "replenishment_detail_id";

    public static final String DATE = "date";

    public static final String START_DATE = "start_date";

    public static final String END_DATE = "end_date";

    public static final String SALES_QTY = "sales_qty";

    public static final String AMOUNT = "amount";

    public static final String CALC_VERSION = "calc_version";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
