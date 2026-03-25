package com.erp.model.wms.entity;

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
 * GB/T2828.1-2012 批量-样本量字码映射表
 * </p>
 *
 * @author zdy
 * @since 2026-03-19
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("qc_sampling_code_rule")
public class QcSamplingCodeRuleEntity extends BaseEntity<QcSamplingCodeRuleEntity> {

    /**
    * 批量下限
    */
    @TableField("min_lot_qty")
    private Integer minLotQty;
    /**
    * 批量上限
    */
    @TableField("max_lot_qty")
    private Integer maxLotQty;
    /**
    * 特殊检验水平S-1字码
    */
    @TableField("code_s1")
    private String codeS1;
    /**
    * 特殊检验水平S-2字码
    */
    @TableField("code_s2")
    private String codeS2;
    /**
    * 特殊检验水平S-3字码
    */
    @TableField("code_s3")
    private String codeS3;
    /**
    * 特殊检验水平S-4字码
    */
    @TableField("code_s4")
    private String codeS4;
    /**
    * 一般检验水平I字码
    */
    @TableField("code_i")
    private String codeI;
    /**
    * 一般检验水平II字码（默认）
    */
    @TableField("code_ii")
    private String codeIi;
    /**
    * 一般检验水平III字码
    */
    @TableField("code_iii")
    private String codeIii;
    /**
    * 样本量字码（A/B/C...R）
    */
    @TableField("sample_qty_code")
    private String sampleQtyCode;
    /**
    * 样本量
    */
    @TableField("sample_qty")
    private Integer sampleQty;
    /**
    * 状态(禁用true启用false)
    */
    @TableField("disabled")
    private Boolean disabled;


    public static final String min_lot_qty = "min_lot_qty";

    public static final String max_lot_qty = "max_lot_qty";

    public static final String CODE_S1 = "code_s1";

    public static final String CODE_S2 = "code_s2";

    public static final String CODE_S3 = "code_s3";

    public static final String CODE_S4 = "code_s4";

    public static final String CODE_I = "code_i";

    public static final String CODE_II = "code_ii";

    public static final String CODE_III = "code_iii";

    public static final String sample_qty_code = "sample_qty_code";

    public static final String sample_qty = "sample_qty";

    public static final String DISABLED = "disabled";

    @Override
    public Serializable pkVal() {
        return null;
    }

}