package com.erp.model.mrp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * <p>
 * 真实断货报告
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("real_out_of_stock")
public class RealOutOfStockEntity extends BaseEntity<RealOutOfStockEntity> {

    /**
     * 补货建议id
     */
    @TableField("replenishment_detail_id")
    private String replenishmentDetailId;

    /**
     * 日期
     */
    @TableField("date")
    private LocalDate date;

    /**
     * 是否断货
     */
    @TableField("is_out_of_stock")
    private Boolean isOutOfStock;

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
