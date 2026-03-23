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
 * GB/T2828.1-2012 AQL判定数主表
 * </p>
 *
 * @author zdy
 * @since 2026-03-19
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("qc_sampling_aql_rule")
public class QcSamplingAqlRuleEntity extends BaseEntity<QcSamplingAqlRuleEntity> {

    /**
    * 样本量字码（A/B/C...R）
    */
    @TableField("sample_qty_code")
    private String sampleQtyCode;
    /**
    * 样本量（与字码绑定）
    */
    @TableField("sample_qty")
    private Integer sampleQty;
    /**
    * AQL值（0.010/0.015/.../100）
    */
    @TableField("aql_value")
    private BigDecimal aqlValue;
    /**
    * 接收数Ac
    */
    @TableField("accept_qty")
    private Integer acceptQty;
    /**
    * 拒收数Re
    */
    @TableField("reject_qty")
    private Integer rejectQty;
    /**
    * 状态(禁用true启用false)
    */
    @TableField("disabled")
    private Boolean disabled;


    public static final String sample_qty_code = "sample_qty_code";

    public static final String sample_qty = "sample_qty";

    public static final String AQL_VALUE = "aql_value";

    public static final String accept_qty = "accept_qty";

    public static final String reject_qty = "reject_qty";

    public static final String DISABLED = "disabled";

    @Override
    public Serializable pkVal() {
        return null;
    }

}