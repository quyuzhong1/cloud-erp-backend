package com.erp.model.wms.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;


/**
 * <p>
 * 
 * </p>
 *
 * @author zdy
 * @since 2026-03-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("qc_sampling_plan_detail")
public class QcSamplingPlanDetailEntity extends BaseEntity<QcSamplingPlanDetailEntity> {

    /**
    * 批量范围从
    */
    @TableField("rang_from")
    private Integer rangFrom;
    /**
    * 批量范围到
    */
    @TableField("rang_to")
    private Integer rangTo;
    /**
    * 抽样比例
    */
    @TableField("rate")
    private BigDecimal rate;
    /**
    * 抽样数量
    */
    @TableField("qty")
    private Integer qty;
    /**
    * 一般缺陷允收数（Ac）
    */
    @TableField("general_accept_qty")
    private Integer generalAcceptQty;
    /**
    * 一般缺陷拒收数(Re)
    */
    @TableField("general_reject_qty")
    private Integer generalRejectQty;
    /**
    * 严重缺陷允收数（Ac）
    */
    @TableField("major_accept_qty")
    private Integer majorAcceptQty;
    /**
    * 严重缺陷拒收数(Re)
    */
    @TableField("major_reject_qty")
    private Integer majorRejectQty;
    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;


    public static final String RANG_FROM = "rang_from";

    public static final String RANG_TO = "rang_to";

    public static final String RATE = "rate";

    public static final String QTY = "qty";

    public static final String GENERAL_ACCEPT_QTY = "general_accept_qty";

    public static final String GENERAL_REJECT_QTY = "general_reject_qty";

    public static final String MAJOR_ACCEPT_QTY = "major_accept_qty";

    public static final String MAJOR_REJECT_QTY = "major_reject_qty";

    public static final String MAIN_ID = "main_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}